package com.github.istin.dmtools.auth.controller;

import com.github.istin.dmtools.auth.service.IntegrationService;
import com.github.istin.dmtools.auth.service.McpConfigurationService;
import com.github.istin.dmtools.auth.model.McpConfiguration;
import com.github.istin.dmtools.dto.IntegrationDto;
import com.github.istin.dmtools.dto.IntegrationConfigDto;
import com.github.istin.dmtools.mcp.generated.MCPSchemaGenerator;
import com.github.istin.dmtools.mcp.generated.MCPToolExecutor;
import com.github.istin.dmtools.di.ServerManagedIntegrationsModule;
import com.github.istin.dmtools.atlassian.jira.BasicJiraClient;
import com.github.istin.dmtools.atlassian.confluence.BasicConfluence;
import com.github.istin.dmtools.atlassian.confluence.Confluence;
import com.github.istin.dmtools.figma.FigmaClient;
import com.github.istin.dmtools.server.util.IntegrationConfigMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.ArrayList;

@RestController
@RequestMapping("/mcp")
public class DynamicMCPController {

    private static final Logger logger = LoggerFactory.getLogger(DynamicMCPController.class);

    private final McpConfigurationService mcpConfigurationService;
    private final IntegrationService integrationService;

    public DynamicMCPController(McpConfigurationService mcpConfigurationService, IntegrationService integrationService) {
        this.mcpConfigurationService = mcpConfigurationService;
        this.integrationService = integrationService;
    }

    @PostMapping(value = "/stream/{configId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter mcpStream(@PathVariable String configId, @RequestBody String body) {
        logger.info("===== MCP Request for configId: {} =====", configId);
        logger.info("Request body: {}", body);

        // Create a new SSE emitter for each request
        SseEmitter emitter = new SseEmitter(30 * 1000L); // 30 second timeout

        emitter.onCompletion(() -> logger.debug("SSE completed for configId: {}", configId));
        emitter.onTimeout(() -> logger.debug("SSE timeout for configId: {}", configId));
        emitter.onError(e -> logger.debug("SSE error for configId: {}", configId, e));

        // Load integration IDs in main thread to avoid LazyInitializationException
        final List<String> integrationIds;
        final String userId;
        try {
            McpConfiguration mcpConfig = mcpConfigurationService.findById(configId);
            logger.info("MCP Configuration lookup result for {}: {}", configId, mcpConfig != null ? "found" : "not found");
            
            if (mcpConfig != null) {
                userId = mcpConfig.getUser().getId();
                integrationIds = new ArrayList<>(mcpConfig.getIntegrationIds()); // Force eager loading
                logger.info("Loaded integration IDs in main thread: {} for user: {}", integrationIds, userId);
            } else {
                userId = null;
                integrationIds = null;
                logger.warn("MCP Configuration not found for configId: {}", configId);
            }
        } catch (Exception e) {
            logger.error("Failed to load MCP configuration in main thread: {}", e.getMessage(), e);
            try {
                emitter.send(SseEmitter.event().data("Error: Failed to load configuration"));
                emitter.complete();
            } catch (Exception sendError) {
                logger.error("Failed to send error response", sendError);
            }
            return emitter;
        }

        // Process request asynchronously with pre-loaded data
        CompletableFuture.runAsync(() -> {
            try {
                JSONObject request = new JSONObject(body);
                String method = request.getString("method");
                Object id = request.opt("id");

                logger.info("Processing method: {} with id: {}", method, id);

                // Handle different MCP methods
                switch (method) {
                    case "initialize":
                        handleInitialize(emitter, id, request.optJSONObject("params"));
                        break;

                    case "tools/list":
                        handleToolsList(emitter, id, configId, userId, integrationIds);
                        break;

                    case "tools/call":
                        handleToolCall(emitter, id, request.optJSONObject("params"), configId, userId, integrationIds);
                        break;

                    case "notifications/initialized":
                        // This is just a notification, acknowledge it but don't send error
                        logger.info("Received initialized notification");
                        break;

                    default:
                        sendError(emitter, id, -32601, "Method not found: " + method);
                        break;
                }

                // Ensure response is sent before completing
                Thread.sleep(100);

        } catch (Exception e) {
                logger.error("Error processing MCP request", e);
                try {
                    emitter.completeWithError(e);
                } catch (Exception ignored) {
                    logger.debug("Emitter already completed");
                }
            } finally {
                // Complete the emitter
                try {
                    emitter.complete();
                } catch (Exception e) {
                    logger.debug("Emitter already completed");
                }
            }
        });

        return emitter;
    }

    private synchronized void sendSseEvent(SseEmitter emitter, String data) throws IOException {
        try {
            emitter.send(SseEmitter.event().data(data));
        } catch (IllegalStateException e) {
            logger.warn("Emitter already completed, skipping send");
            throw e;
        }
    }

    private void handleInitialize(SseEmitter emitter, Object id, JSONObject params) throws Exception {
        String clientProtocolVersion = params != null ?
                params.optString("protocolVersion", "2025-07-27") : "2025-07-27";

        JSONObject response = new JSONObject()
                .put("jsonrpc", "2.0")
                .put("id", id)
                .put("result", new JSONObject()
                        .put("protocolVersion", clientProtocolVersion)
                        .put("capabilities", new JSONObject()
                                .put("tools", new JSONObject()))
                        .put("serverInfo", new JSONObject()
                                .put("name", "dmtools-mcp-server")
                                .put("version", "1.0.0")));

        logger.info("Sending initialize response");
        sendSseEvent(emitter, response.toString());
    }

    private void handleToolsList(SseEmitter emitter, Object id, String configId, String userId, List<String> integrationIds) throws Exception {
        // Check if we have valid MCP configuration
        if (userId == null || integrationIds == null) {
            logger.warn("Could not find MCP configuration for configId: {}. Using hello world tool as fallback", configId);
            // Fallback to hello world tool if no configuration found
            JSONArray tools = getHelloWorldTool();
            
            JSONObject response = new JSONObject()
                    .put("jsonrpc", "2.0")
                    .put("id", id)
                    .put("result", new JSONObject()
                            .put("tools", tools));

            logger.info("Sending tools/list response with {} fallback tools", tools.length());
            sendSseEvent(emitter, response.toString());
            return;
        }
        
        logger.info("MCP Configuration {} has integration IDs: {} for user: {}", configId, integrationIds, userId);

        JSONArray tools;
        if (integrationIds.isEmpty()) {
            logger.warn("No integrations configured for MCP config {}, using hello world tool as fallback", configId);
            // Fallback to hello world tool if no integrations configured
            tools = getHelloWorldTool();
        } else {
            // Get integration types for the configured integration IDs
            Set<String> configuredIntegrationTypes = new HashSet<>();
            for (String integrationId : integrationIds) {
                try {
                    IntegrationDto integrationDto = integrationService.getIntegrationById(integrationId, userId, false);
                    configuredIntegrationTypes.add(integrationDto.getType());
                    logger.debug("Resolved integration ID {} to type: {}", integrationId, integrationDto.getType());
                } catch (Exception e) {
                    logger.error("Failed to resolve integration ID {}: {}", integrationId, e.getMessage());
                }
            }

            logger.info("MCP Configuration {} has integration types: {}", configId, configuredIntegrationTypes);

            if (configuredIntegrationTypes.isEmpty()) {
                logger.warn("No valid integrations found for MCP config {}, using hello world tool as fallback", configId);
                tools = getHelloWorldTool();
            } else {
                // Generate actual tools based on configured integrations only
                Map<String, Object> toolsList = MCPSchemaGenerator.generateToolsListResponse(configuredIntegrationTypes);
                List<Map<String, Object>> toolsFromGenerator = (List<Map<String, Object>>) toolsList.get("tools");
                
                tools = new JSONArray();
                for (Map<String, Object> tool : toolsFromGenerator) {
                    tools.put(new JSONObject(tool));
                }
                logger.info("Generated {} tools from MCP configuration integrations: {}", tools.length(), configuredIntegrationTypes);
            }
        }

        JSONObject response = new JSONObject()
                .put("jsonrpc", "2.0")
                .put("id", id)
                .put("result", new JSONObject()
                        .put("tools", tools));

        logger.info("Sending tools/list response with {} tools", tools.length());
        sendSseEvent(emitter, response.toString());
    }

    private JSONArray getHelloWorldTool() {
        return new JSONArray()
                .put(new JSONObject()
                        .put("name", "hello_world")
                        .put("description", "A simple hello world tool (fallback when no integrations available)")
                        .put("inputSchema", new JSONObject()
                                .put("type", "object")
                                .put("properties", new JSONObject()
                                        .put("name", new JSONObject()
                                                .put("type", "string")
                                                .put("description", "Your name")
                                                .put("default", "World")))
                                .put("required", new JSONArray())));
    }

    private void handleToolCall(SseEmitter emitter, Object id, JSONObject params, String configId, String userId, List<String> integrationIds) throws Exception {
        String toolName = params.optString("name");
        JSONObject arguments = params.optJSONObject("arguments");

        logger.info("Tool call: {} with arguments: {}", toolName, arguments);

        // Handle hello world tool (fallback)
        if ("hello_world".equals(toolName)) {
            handleHelloWorldTool(emitter, id, arguments);
            return;
        }

        // Check if we have valid MCP configuration
        if (userId == null || integrationIds == null || integrationIds.isEmpty()) {
            logger.warn("No valid MCP configuration found for configId: {}. Falling back to hello world tool.", configId);
            // Fall back to hello world tool with a message
            String fallbackMessage = "No MCP configuration found. Please create an MCP configuration with integrations first.";
            JSONObject response = new JSONObject()
                    .put("jsonrpc", "2.0")
                    .put("id", id)
                    .put("result", new JSONObject()
                            .put("content", new JSONArray()
                                    .put(new JSONObject()
                                            .put("type", "text")
                                            .put("text", fallbackMessage))));
            logger.info("Sending fallback response for missing MCP configuration");
            sendSseEvent(emitter, response.toString());
            return;
        }

        // Handle actual tools with custom execution for server-managed integrations
        try {
            // Get client instances for the user
            Map<String, Object> clientInstances = createClientInstances(configId, userId, integrationIds);
            
            // Convert JSONObject arguments to Map
            Map<String, Object> argumentsMap = new HashMap<>();
            if (arguments != null) {
                arguments.keys().forEachRemaining(key -> {
                    argumentsMap.put(key, arguments.get(key));
                });
            }

            // Execute tools with custom logic for server-managed integrations
            Object result = executeServerManagedTool(toolName, argumentsMap, clientInstances);
            
            // Process result based on type - handle File objects specially for MCP protocol
            JSONObject response;
            if (result instanceof File) {
                response = handleFileResult(emitter, id, (File) result);
            } else {
                // Convert result to string for text response
                String resultText = (result != null) ? result.toString() : "Tool executed successfully but returned no result.";

                response = new JSONObject()
                        .put("jsonrpc", "2.0")
                        .put("id", id)
                        .put("result", new JSONObject()
                                .put("content", new JSONArray()
                                        .put(new JSONObject()
                                                .put("type", "text")
                                                .put("text", resultText))));
            }

            logger.info("Sending tool execution result for: {}", toolName);
            sendSseEvent(emitter, response.toString());

        } catch (Exception e) {
            logger.error("Failed to execute tool: {} with error: {}", toolName, e.getMessage(), e);
            sendError(emitter, id, -32603, "Tool execution failed: " + e.getMessage());
        }
    }

    /**
     * Custom tool execution logic for server-managed integrations.
     * Handles type conversions and compatibility with server-managed clients.
     */
    private Object executeServerManagedTool(String toolName, Map<String, Object> arguments, Map<String, Object> clientInstances) throws Exception {
        switch (toolName) {
            case "confluence_find_content":
                return executeConfluenceToolWithServerManagedClient(arguments, clientInstances);
            
            case "jira_get_text_fields":
                return executeJiraToolWithServerManagedClient(arguments, clientInstances);
                
            default:
                return MCPToolExecutor.executeTool(toolName, arguments, clientInstances);
        }
    }
    
    /**
     * Execute Confluence tool with server-managed client.
     */
    private Object executeConfluenceToolWithServerManagedClient(Map<String, Object> arguments, Map<String, Object> clientInstances) throws Exception {
        Object confluenceClient = clientInstances.get("confluence");
        if (confluenceClient == null) {
            throw new IllegalArgumentException("Confluence client not available");
        }
        
        String title = (String) arguments.get("title");
        if (title == null) {
            throw new IllegalArgumentException("Required parameter 'title' is missing");
        }
        
        // Use the Confluence interface methods (available on both BasicConfluence and CustomServerManagedConfluence)
        if (confluenceClient instanceof Confluence) {
            Confluence confluence = (Confluence) confluenceClient;
            // Use findContent with default space (null means search all spaces)
            return confluence.findContent(title, "AINA");
        } else {
            throw new IllegalArgumentException("Invalid Confluence client type: " + confluenceClient.getClass().getName());
        }
    }
    
    /**
     * Execute Jira tool with server-managed client and JSONObject to string conversion.
     */
    private Object executeJiraToolWithServerManagedClient(Map<String, Object> arguments, Map<String, Object> clientInstances) throws Exception {
        Object jiraClient = clientInstances.get("jira");
        if (jiraClient == null) {
            throw new IllegalArgumentException("Jira client not available");
        }
        
        Object ticketObject = arguments.get("ticket");
        if (ticketObject == null) {
            throw new IllegalArgumentException("Required parameter 'ticket' is missing");
        }
        
        // Convert JSONObject to a text fields representation
        if (ticketObject instanceof JSONObject) {
            JSONObject ticketJson = (JSONObject) ticketObject;
            String key = ticketJson.optString("key", "UNKNOWN");
            String summary = ticketJson.optString("summary", "");
            String description = ticketJson.optString("description", "");
            
            // For MCP tools, just return the text fields directly instead of using complex ITicket
            StringBuilder result = new StringBuilder();
            if (!summary.isEmpty()) {
                result.append("Title: ").append(summary).append("\n");
            }
            if (!description.isEmpty()) {
                result.append("Description: ").append(description).append("\n");
            }
            if (!key.isEmpty()) {
                result.append("Key: ").append(key).append("\n");
            }
            
            return result.toString().trim();
        } else {
            throw new IllegalArgumentException("Expected JSONObject for ticket parameter, got: " + ticketObject.getClass().getName());
        }
    }
    
    // Remove the SimpleTicket class completely since we're not using it anymore

    /**
     * Creates client instances for the given MCP configuration.
     * Uses the same pattern as JobExecutionController.
     */
    private Map<String, Object> createClientInstances(String configId, String userId, List<String> integrationIds) throws Exception {
        if (userId == null || integrationIds == null) {
            throw new IllegalArgumentException("User ID or integration IDs not available");
        }

        logger.info("Creating client instances for user {} with integrations: {}", userId, integrationIds);

        // Resolve integrations from IDs (same as JobExecutionController)
        JSONObject resolvedIntegrations = resolveIntegrationIds(integrationIds, userId);
        
        logger.info("Resolved integrations: {}", resolvedIntegrations.keySet());

        // Create ServerManagedIntegrationsModule with resolved integrations
        ServerManagedIntegrationsModule integrationsModule = new ServerManagedIntegrationsModule(resolvedIntegrations);
        
        Map<String, Object> clientInstances = new HashMap<>();
        
        // Create Jira client if available
        if (resolvedIntegrations.has("jira")) {
            try {
                Object jiraClient = integrationsModule.provideTrackerClient();
                clientInstances.put("jira", jiraClient);
                logger.info("Created Jira client instance");
            } catch (Exception e) {
                logger.error("Failed to create Jira client: {}", e.getMessage());
            }
        }

        // Create Confluence client if available
        if (resolvedIntegrations.has("confluence")) {
            try {
                Confluence confluenceClient = integrationsModule.provideConfluence();
                clientInstances.put("confluence", confluenceClient);
                logger.info("Created Confluence client instance");
            } catch (Exception e) {
                logger.error("Failed to create Confluence client: {}", e.getMessage());
            }
        }

        // Create Figma client if available
        if (resolvedIntegrations.has("figma")) {
            try {
                FigmaClient figmaClient = integrationsModule.provideFigmaClient();
                clientInstances.put("figma", figmaClient);
                logger.info("Created Figma client instance");
            } catch (Exception e) {
                logger.error("Failed to create Figma client: {}", e.getMessage());
            }
        }
        
        return clientInstances;
    }

    /**
     * Resolves integration IDs to JSONObject configuration.
     * Uses the common IntegrationConfigMapper utility.
     */
    private JSONObject resolveIntegrationIds(List<String> integrationIds, String userId) {
        JSONObject resolved = new JSONObject();
        
        for (String integrationId : integrationIds) {
            try {
                logger.info("Resolving integration ID: {}", integrationId);
                
                // Get integration configuration from database with sensitive data
                IntegrationDto integrationDto = integrationService.getIntegrationById(integrationId, userId, true);
                
                // Convert to JSONObject format expected by ServerManagedIntegrationsModule using common utility
                JSONObject integrationConfig = IntegrationConfigMapper.mapIntegrationConfig(integrationDto);
                
                // Use integration type as key
                resolved.put(integrationDto.getType(), integrationConfig);
                
                logger.info("Successfully resolved integration ID '{}' as type '{}'", integrationId, integrationDto.getType());
                
                // Record usage
                integrationService.recordIntegrationUsage(integrationId);
                
            } catch (Exception e) {
                logger.error("Failed to resolve integration ID '{}': {}", integrationId, e.getMessage(), e);
            }
        }
        
        return resolved;
    }

    private void handleHelloWorldTool(SseEmitter emitter, Object id, JSONObject arguments) throws Exception {
        String name = arguments != null ? arguments.optString("name", "World") : "World";

        JSONObject response = new JSONObject()
                .put("jsonrpc", "2.0")
                .put("id", id)
                .put("result", new JSONObject()
                        .put("content", new JSONArray()
                                .put(new JSONObject()
                                        .put("type", "text")
                                        .put("text", "Hello, " + name + "! This is a response from the DMTools MCP server (fallback mode)."))));

        logger.info("Sending hello world tool response");
        sendSseEvent(emitter, response.toString());
    }

    private void sendError(SseEmitter emitter, Object id, int code, String message) {
        try {
            JSONObject response = new JSONObject()
                    .put("jsonrpc", "2.0")
                    .put("id", id)
                    .put("error", new JSONObject()
                            .put("code", code)
                            .put("message", message));

            logger.error("Sending error response: {}", response.toString(2));
            sendSseEvent(emitter, response.toString());
        } catch (Exception e) {
            logger.error("Failed to send error", e);
        }
    }

    /**
     * Handles File objects by converting them to base64 content for MCP protocol compliance.
     * Uses memory-efficient streaming encoding for large files.
     */
    private JSONObject handleFileResult(SseEmitter emitter, Object id, File file) throws Exception {
        logger.info("Processing File result: {} (size: {} bytes)", file.getName(), file.length());
        
        // Check file size limits (protect against very large files)
        final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB limit
        if (file.length() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File too large for MCP transmission: " + file.length() + " bytes (max: " + MAX_FILE_SIZE + ")");
        }
        
        String base64Content;
        String mimeType = determineMimeType(file);
        
        try {
            // For small files (< 10MB), read directly
            if (file.length() < 10 * 1024 * 1024) {
                base64Content = Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath()));
            } else {
                // For large files, use streaming approach
                base64Content = encodeFileToBase64Streaming(file);
            }
            
            logger.info("Successfully encoded file {} to base64 (original: {} bytes, encoded length: {} chars)", 
                file.getName(), file.length(), base64Content.length());
            
        } catch (Exception e) {
            logger.error("Failed to encode file {} to base64: {}", file.getName(), e.getMessage(), e);
            throw new RuntimeException("Failed to encode file to base64: " + e.getMessage(), e);
        } finally {
            // Clean up the temporary file immediately after encoding
            try {
                if (file.delete()) {
                    logger.debug("Successfully deleted temporary file: {}", file.getName());
                } else {
                    logger.warn("Failed to delete temporary file: {}", file.getName());
                }
            } catch (Exception e) {
                logger.warn("Error deleting temporary file {}: {}", file.getName(), e.getMessage());
            }
        }
        
        // Return MCP-compliant JSON response with embedded file content
        return new JSONObject()
                .put("jsonrpc", "2.0")
                .put("id", id)
                .put("result", new JSONObject()
                        .put("content", new JSONArray()
                                .put(new JSONObject()
                                        .put("type", "file")
                                        .put("content", base64Content)
                                        .put("filename", file.getName())
                                        .put("mimeType", mimeType))));
    }
    
    /**
     * Encodes a file to base64 using streaming approach for memory efficiency.
     * Processes file in 8KB chunks to minimize memory usage.
     */
    private String encodeFileToBase64Streaming(File file) throws IOException {
        final int BUFFER_SIZE = 8192; // 8KB chunks
        StringBuilder base64Builder = new StringBuilder();
        
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file), BUFFER_SIZE)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            
            while ((bytesRead = bis.read(buffer)) != -1) {
                // Encode each chunk separately
                byte[] chunk = new byte[bytesRead];
                System.arraycopy(buffer, 0, chunk, 0, bytesRead);
                base64Builder.append(Base64.getEncoder().encodeToString(chunk));
            }
        }
        
        return base64Builder.toString();
    }
    
    /**
     * Determines MIME type for the file based on extension.
     */
    private String determineMimeType(File file) {
        String fileName = file.getName().toLowerCase();
        if (fileName.endsWith(".png")) {
            return "image/png";
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (fileName.endsWith(".gif")) {
            return "image/gif";
        } else if (fileName.endsWith(".svg")) {
            return "image/svg+xml";
        } else if (fileName.endsWith(".pdf")) {
            return "application/pdf";
        } else {
            return "application/octet-stream";
        }
    }
} 