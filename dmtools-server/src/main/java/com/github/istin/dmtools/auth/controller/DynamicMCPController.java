package com.github.istin.dmtools.auth.controller;

import com.github.istin.dmtools.auth.service.IntegrationService;
import com.github.istin.dmtools.server.service.IntegrationResolutionHelper;
import com.github.istin.dmtools.auth.service.McpConfigurationService;
import com.github.istin.dmtools.auth.service.IntegrationConfigurationLoader;
import com.github.istin.dmtools.auth.model.McpConfiguration;
import com.github.istin.dmtools.auth.model.jsonrpc.JsonRpcRequest;
import com.github.istin.dmtools.auth.model.jsonrpc.JsonRpcResponse;
import com.github.istin.dmtools.auth.model.jsonrpc.JsonRpcError;
import com.github.istin.dmtools.auth.model.jsonrpc.JsonRpcMethods;

import com.github.istin.dmtools.auth.model.mcp.McpInitializeResponse;
import com.github.istin.dmtools.auth.model.mcp.McpToolsListResponse;
import com.github.istin.dmtools.auth.model.mcp.McpToolCallResponse;
import com.github.istin.dmtools.dto.IntegrationDto;
import com.github.istin.dmtools.mcp.generated.MCPSchemaGenerator;
import com.github.istin.dmtools.mcp.generated.MCPToolExecutor;
import com.github.istin.dmtools.di.ServerManagedIntegrationsModule;
import com.github.istin.dmtools.atlassian.confluence.Confluence;
import com.github.istin.dmtools.figma.FigmaClient;
import com.github.istin.dmtools.server.util.IntegrationConfigMapper;
import com.github.istin.dmtools.server.service.FileDownloadService;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;

@RestController
@RequestMapping("/mcp")
public class DynamicMCPController {

    private static final Logger logger = LoggerFactory.getLogger(DynamicMCPController.class);

    // ===== LOGGING HELPER METHODS =====

    private void logRequestStart(String endpoint, String configId, String method, String body) {
        if (logger.isInfoEnabled()) {
            logger.info("===== MCP {} Request for configId: {} =====", endpoint, configId);
            if (method != null) {
                logger.info("🎯 Method: {}, Body: {}", method, body != null ? body.substring(0, Math.min(100, body.length())) + (body.length() > 100 ? "..." : "") : "null");
            }
        }
    }

    private void logRequestAnalysis(HttpServletRequest request, String configId) {
        if (logger.isDebugEnabled()) {
            logger.debug("=== Request Analysis for {} ===", configId);
            logger.debug("Method: {}, Path: {}, Content-Type: {}", 
                request.getMethod(), request.getRequestURI(), request.getContentType());
            logger.debug("User-Agent: {}", request.getHeader("User-Agent"));
        }
    }

    private void logMethodProcessing(String transport, String method, Object id, int toolCount) {
        if (logger.isInfoEnabled()) {
            if (toolCount > 0) {
                logger.info("🎯 {} - {} completed with {} tools", transport, method, toolCount);
            } else {
                logger.info("🎯 {} - {} completed", transport, method);
            }
        }
    }


    private void logError(String operation, Exception e) {
        logger.error("Error in {}: {}", operation, e.getMessage());
        if (logger.isDebugEnabled()) {
            logger.debug("Full stack trace for " + operation, e);
        }
    }

    private void logConfigurationResolution(String configId, List<String> integrationIds, String userId, Set<String> types) {
        if (logger.isInfoEnabled()) {
            logger.info("Configuration {} resolved: {} integrations for user {}, types: {}", 
                configId, integrationIds.size(), userId, types);
        }
    }

    private final McpConfigurationService mcpConfigurationService;
    private final IntegrationService integrationService;
    private final IntegrationConfigurationLoader configurationLoader;
    private final FileDownloadService fileDownloadService;
    private final IntegrationResolutionHelper integrationResolutionHelper;

    public DynamicMCPController(McpConfigurationService mcpConfigurationService, IntegrationService integrationService, IntegrationConfigurationLoader configurationLoader, FileDownloadService fileDownloadService, IntegrationResolutionHelper integrationResolutionHelper) {
        this.mcpConfigurationService = mcpConfigurationService;
        this.integrationService = integrationService;
        this.configurationLoader = configurationLoader;
        this.fileDownloadService = fileDownloadService;
        this.integrationResolutionHelper = integrationResolutionHelper;
    }


    /**
     * Core business logic for initialize - unified for both SSE and JSON-RPC
     */
    private McpInitializeResponse processInitialize(JSONObject params) {
        String clientProtocolVersion = params != null ? params.optString("protocolVersion", "2025-01-08") : "2025-01-08";
        return McpInitializeResponse.defaultResponse(clientProtocolVersion);
    }

    /**
     * Core business logic for generating tools list - unified for both SSE and JSON-RPC
     */
    private McpToolsListResponse generateToolsList(String configId) throws Exception {
        // Get MCP configuration
        McpConfiguration mcpConfig = mcpConfigurationService.findById(configId);
        if (mcpConfig == null) {
            throw new IllegalArgumentException("No configuration found for configId " + configId);
        }

        String userId = mcpConfig.getUser().getId();
        List<String> integrationIds = new ArrayList<>(mcpConfig.getIntegrationIds());

        if (integrationIds.isEmpty()) {
            throw new IllegalArgumentException("No integration IDs found for configId " + configId);
        }

        // Get integration types for the configured integration IDs
        Set<String> configuredIntegrationTypes = new HashSet<>();
        for (String integrationId : integrationIds) {
            try {
                IntegrationDto integrationDto = integrationService.getIntegrationById(integrationId, userId, false);
                configuredIntegrationTypes.add(integrationDto.getType());
            } catch (Exception e) {
                // Log error but continue with other integrations
                if (logger.isDebugEnabled()) {
                    logger.debug("Failed to resolve integration ID {}: {}", integrationId, e.getMessage());
                }
            }
        }

        // Use the helper method for logging
        logConfigurationResolution(configId, integrationIds, userId, configuredIntegrationTypes);

        if (configuredIntegrationTypes.isEmpty()) {
            throw new IllegalArgumentException("No valid integrations found for configId " + configId);
        }

        // Generate actual tools based on configured integrations only
        Map<String, Object> toolsList = MCPSchemaGenerator.generateToolsListResponse(configuredIntegrationTypes);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> toolsFromGenerator = (List<Map<String, Object>>) toolsList.get("tools");

        return new McpToolsListResponse(toolsFromGenerator);
    }

    /**
     * Core business logic for tool calls - unified for both SSE and JSON-RPC
     */
    private McpToolCallResponse processToolCall(String configId, String toolName, JSONObject arguments, HttpServletRequest request) throws Exception {
        // Validate tool name
        if (toolName == null || toolName.isEmpty()) {
            throw new IllegalArgumentException("missing tool name");
        }

        // Get MCP configuration to validate access
        McpConfiguration mcpConfig = mcpConfigurationService.findById(configId);
        if (mcpConfig == null) {
            throw new IllegalArgumentException("No configuration found for configId " + configId);
        }

        // Execute the tool via the existing MCP tool execution logic - returns raw Object result
        Object result = executeToolCallRaw(configId, toolName, arguments, mcpConfig);
        
        // Handle File results with proper JSON response
        if (result instanceof File) {
            File file = (File) result;
            String downloadToken = fileDownloadService.createDownloadToken(file);
            
            // Build full URL using request context
            String scheme = request.getScheme();
            String serverName = request.getServerName();
            int serverPort = request.getServerPort();
            String contextPath = request.getContextPath();
            
            String downloadUrl;
            if ((scheme.equals("http") && serverPort == 80) || (scheme.equals("https") && serverPort == 443)) {
                downloadUrl = String.format("%s://%s%s/api/files/download/%s", scheme, serverName, contextPath, downloadToken);
            } else {
                downloadUrl = String.format("%s://%s:%d%s/api/files/download/%s", scheme, serverName, serverPort, contextPath, downloadToken);
            }
            
            String mimeType = determineMimeType(file);
            return McpToolCallResponse.file(downloadUrl, file.getName(), mimeType, "15 minutes");
        }
        
        // Handle regular string results
        String textResult = (result != null) ? result.toString() : "Tool executed successfully but returned no result.";
        return McpToolCallResponse.text(textResult);
    }

    private Object executeToolCallRaw(String configId, String toolName, JSONObject arguments, McpConfiguration mcpConfig) {
        try {
            String userId = mcpConfig.getUser().getId();
            List<String> integrationIds = new ArrayList<>(mcpConfig.getIntegrationIds());
            
            // Load user and integration details in main thread to avoid LazyInitializationException
            Set<String> configuredIntegrationTypes = new HashSet<>();
            for (String integrationId : integrationIds) {
                try {
                    IntegrationDto integrationDto = integrationService.getIntegrationById(integrationId, userId, false);
                    configuredIntegrationTypes.add(integrationDto.getType());
                } catch (Exception e) {
                    logger.error("Failed to resolve integration ID {}: {}", integrationId, e.getMessage());
                }
            }

            if (configuredIntegrationTypes.isEmpty()) {
                throw new RuntimeException("No valid integrations found for MCP config " + configId);
            }

            logger.info("Executing tool {} for integrations: {}", toolName, configuredIntegrationTypes);
            
            // Use the same tool execution logic as the SSE implementation
            Map<String, Object> clientInstances = createClientInstances(userId, integrationIds);
            
            // Convert JSONObject arguments to Map
            Map<String, Object> argumentsMap = new HashMap<>();
            if (arguments != null) {
                arguments.keys().forEachRemaining(key -> {
                    argumentsMap.put(key, arguments.get(key));
                });
            }

            // Execute the tool using the same logic as SSE implementation
            // Return the raw result (Object) - could be File, String, etc.
            return executeServerManagedTool(toolName, argumentsMap, clientInstances);
            
        } catch (Exception e) {
            logger.error("Error in executeToolCallRaw", e);
            throw new RuntimeException("Failed to execute tool: " + e.getMessage(), e);
        }
    }

    @GetMapping(value = "/stream/{configId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter mcpStreamGet(@PathVariable String configId, HttpServletRequest request) {
        logRequestStart("SSE GET", configId, "GET", null);
        logRequestAnalysis(request, configId);
        
        // Default initialization for SSE GET requests
        JsonRpcRequest initRequest = new JsonRpcRequest(JsonRpcMethods.INITIALIZE, "init-get", new JSONObject());
        return mcpStreamPost(configId, initRequest.toString(), request);
    }

    @PostMapping(value = "/stream/{configId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter mcpStreamPost(@PathVariable String configId, @RequestBody String body, HttpServletRequest request) {
        logRequestStart("SSE POST", configId, "POST", body);
        logRequestAnalysis(request, configId);

        SseEmitter emitter = new SseEmitter();

        // Process SSE request directly (no need for additional async wrapper)
        try {
            JSONObject requestJson = new JSONObject(body);
            String method = requestJson.getString("method");
            Object id = requestJson.opt("id");

            // Handle different MCP methods
            switch (method) {
                case JsonRpcMethods.INITIALIZE:
                    handleInitialize(emitter, id, requestJson.optJSONObject("params"), configId, request);
                    break;

                case JsonRpcMethods.TOOLS_LIST:
                    handleToolsList(emitter, id, configId);
                    break;

                case JsonRpcMethods.TOOLS_CALL:
                    handleToolCall(emitter, id, requestJson.optJSONObject("params"), configId, request);
                    break;

                case JsonRpcMethods.NOTIFICATIONS_INITIALIZED:
                    // This is just a notification - no response needed
                    break;

                default:
                    sendError(emitter, id, -32601, "Method not found: " + method);
                    break;
            }

        } catch (Exception e) {
            logError("SSE processing", e);
            try {
                sendError(emitter, null, -32603, "Internal error: " + e.getMessage());
            } catch (Exception sendError) {
                logError("SSE error response", sendError);
            }
        } finally {
            // Complete the emitter to close SSE connection
            try {
                emitter.complete();
            } catch (Exception ignored) {
                // Emitter already completed
            }
        }

        return emitter;
    }
    
    private void sendSseEvent(SseEmitter emitter, String data) throws IOException {
        try {
            emitter.send(SseEmitter.event().data(data));
        } catch (IllegalStateException e) {
            logger.warn("Emitter already completed, skipping send");
            throw e;
        }
    }

    private void handleInitialize(SseEmitter emitter, Object id, JSONObject params, String configId, HttpServletRequest request) throws Exception {
        McpInitializeResponse initResponse = processInitialize(params);
        JsonRpcResponse response = JsonRpcResponse.success(id, initResponse.toJson());

        sendSseEvent(emitter, response.toString());
        
        // For Gemini CLI (user-agent: node), auto-send tools/list after initialize over SSE
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null && userAgent.contains("node")) {
            handleToolsList(emitter, "auto-tools-list", configId);
        }
    }

    private void handleToolsList(SseEmitter emitter, Object id, String configId) throws Exception {
        try {
            McpToolsListResponse toolsResponse = generateToolsList(configId);
            JsonRpcResponse response = JsonRpcResponse.success(id, toolsResponse.toJson());
            sendSseEvent(emitter, response.toString());
        } catch (IllegalArgumentException e) {
            logError("SSE tools/list - invalid params", e);
            sendError(emitter, id, JsonRpcError.INVALID_PARAMS, "Invalid params: " + e.getMessage());
        } catch (Exception e) {
            logError("SSE tools/list", e);
            sendError(emitter, id, JsonRpcError.INTERNAL_ERROR, "Internal error: " + e.getMessage());
        }
    }

    private void handleToolCall(SseEmitter emitter, Object id, JSONObject params, String configId, HttpServletRequest request) throws Exception {
        try {
            String toolName = params.optString("name");
            JSONObject arguments = params.optJSONObject("arguments");

            // Use unified business logic with request context for file handling
            McpToolCallResponse toolResponse = processToolCall(configId, toolName, arguments, request);
            JsonRpcResponse response = JsonRpcResponse.success(id, toolResponse.toJson());

            sendSseEvent(emitter, response.toString());

        } catch (IllegalArgumentException e) {
            logError("SSE tools/call - invalid params", e);
            sendError(emitter, id, JsonRpcError.INVALID_PARAMS, "Invalid params: " + e.getMessage());
        } catch (Exception e) {
            logError("SSE tools/call", e);
            sendError(emitter, id, JsonRpcError.INTERNAL_ERROR, "Internal error: " + e.getMessage());
        }
    }

    /**
     * Determine MIME type based on file extension
     */
    private String determineMimeType(File file) {
        String fileName = file.getName().toLowerCase();
        if (fileName.endsWith(".pdf")) {
            return "application/pdf";
        } else if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        } else if (fileName.endsWith(".docx") || fileName.endsWith(".doc")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        } else if (fileName.endsWith(".csv")) {
            return "text/csv";
        } else if (fileName.endsWith(".txt")) {
            return "text/plain";
        } else if (fileName.endsWith(".json")) {
            return "application/json";
        } else if (fileName.endsWith(".xml")) {
            return "application/xml";
        } else if (fileName.endsWith(".zip")) {
            return "application/zip";
        } else if (fileName.endsWith(".png")) {
            return "image/png";
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else {
            return "application/octet-stream";
        }
    }

    /**
     * Custom tool execution logic for server-managed integrations.
     * Handles type conversions and compatibility with server-managed clients.
     */
    private Object executeServerManagedTool(String toolName, Map<String, Object> arguments, Map<String, Object> clientInstances) throws Exception {
        return MCPToolExecutor.executeTool(toolName, arguments, clientInstances);
    }
    
    /**
     * Creates client instances for the given MCP configuration.
     * Uses the same pattern as JobExecutionController.
     */
    private Map<String, Object> createClientInstances(String userId, List<String> integrationIds) throws Exception {
        if (userId == null || integrationIds == null) {
            throw new IllegalArgumentException("User ID or integration IDs not available");
        }

        logger.info("Creating client instances for user {} with integrations: {}", userId, integrationIds);

        // Resolve integrations from IDs (same as JobExecutionController)
        JSONObject resolvedIntegrations = integrationResolutionHelper.resolveIntegrationIds(integrationIds, userId);
        
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



    private void sendError(SseEmitter emitter, Object id, int code, String message) {
        try {
            JsonRpcResponse response = JsonRpcResponse.error(id, new JsonRpcError(code, message));

            logger.error("Sending error response: {}", response);
            sendSseEvent(emitter, response.toString());
        } catch (Exception e) {
            logger.error("Failed to send error", e);
        }
    }

    /**
     * Handles File objects by creating a secure one-time download URL.
     * Returns a download URL that expires in 15 minutes and can only be used once.
     */
    private JsonRpcResponse handleFileResult(Object id, File file, HttpServletRequest request) throws Exception {
        logger.info("Processing File result: {} (size: {} bytes)", file.getName(), file.length());
        
        // Check file size limits (protect against very large files)
        final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB limit
        if (file.length() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File too large for MCP transmission: " + file.length() + " bytes (max: " + MAX_FILE_SIZE + ")");
        }
        
        try {
            // Create a secure one-time download token
            String downloadToken = fileDownloadService.createDownloadToken(file);
            
            // Build full URL using request context
            String scheme = request.getScheme();
            String serverName = request.getServerName();
            int serverPort = request.getServerPort();
            String contextPath = request.getContextPath();
            
            String downloadUrl;
            if ((scheme.equals("http") && serverPort == 80) || (scheme.equals("https") && serverPort == 443)) {
                downloadUrl = String.format("%s://%s%s/api/files/download/%s", scheme, serverName, contextPath, downloadToken);
            } else {
                downloadUrl = String.format("%s://%s:%d%s/api/files/download/%s", scheme, serverName, serverPort, contextPath, downloadToken);
            }
            
            logger.info("Created secure download URL for file {}: {}", file.getName(), downloadUrl);
            
            // Create file response using new models
            McpToolCallResponse fileResponse = McpToolCallResponse.file(
                    downloadUrl, 
                    file.getName(), 
                    determineMimeType(file), 
                    "15 minutes"
            );
            
            // Return JSON-RPC response
            return JsonRpcResponse.success(id, fileResponse.toJson());
            
        } catch (Exception e) {
            logger.error("Failed to create download URL for file {}: {}", file.getName(), e.getMessage(), e);
            throw new RuntimeException("Failed to create download URL: " + e.getMessage(), e);
        }
        // Note: File will be cleaned up by FileDownloadService after download or expiration
    }
    

    
    /**
     * Determines MIME type for the file based on extension.
     */
} 