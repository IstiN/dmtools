package com.github.istin.dmtools.server.service;

import com.github.istin.dmtools.atlassian.confluence.Confluence;
import com.github.istin.dmtools.auth.model.McpConfiguration;
import com.github.istin.dmtools.auth.model.mcp.McpToolCallResponse;
import com.github.istin.dmtools.auth.model.mcp.McpToolsListResponse;
import com.github.istin.dmtools.auth.service.IntegrationService;
import com.github.istin.dmtools.auth.service.McpConfigurationService;
import com.github.istin.dmtools.di.ServerManagedIntegrationsModule;
import com.github.istin.dmtools.dto.IntegrationDto;
import com.github.istin.dmtools.figma.FigmaClient;
import com.github.istin.dmtools.mcp.generated.MCPSchemaGenerator;
import com.github.istin.dmtools.mcp.generated.MCPToolExecutor;
import jakarta.servlet.http.HttpServletRequest;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;

/**
 * Utility service for MCP configuration resolution and tool execution.
 * Provides common functionality for both ChatService and DynamicMCPController.
 */
@Service
public class McpConfigurationResolverService {

    private static final Logger logger = LoggerFactory.getLogger(McpConfigurationResolverService.class);

    private final McpConfigurationService mcpConfigurationService;
    private final IntegrationService integrationService;
    private final IntegrationResolutionHelper integrationResolutionHelper;
    private final FileDownloadService fileDownloadService;

    public McpConfigurationResolverService(
            McpConfigurationService mcpConfigurationService,
            IntegrationService integrationService,
            IntegrationResolutionHelper integrationResolutionHelper,
            FileDownloadService fileDownloadService) {
        this.mcpConfigurationService = mcpConfigurationService;
        this.integrationService = integrationService;
        this.integrationResolutionHelper = integrationResolutionHelper;
        this.fileDownloadService = fileDownloadService;
    }

    /**
     * Resolves MCP configuration and extracts integration details.
     */
    public static class McpConfigurationResult {
        private final McpConfiguration mcpConfig;
        private final String userId;
        private final List<String> integrationIds;
        private final Set<String> integrationTypes;

        public McpConfigurationResult(McpConfiguration mcpConfig, String userId, 
                                    List<String> integrationIds, Set<String> integrationTypes) {
            this.mcpConfig = mcpConfig;
            this.userId = userId;
            this.integrationIds = integrationIds;
            this.integrationTypes = integrationTypes;
        }

        public McpConfiguration getMcpConfig() { return mcpConfig; }
        public String getUserId() { return userId; }
        public List<String> getIntegrationIds() { return integrationIds; }
        public Set<String> getIntegrationTypes() { return integrationTypes; }
    }

    /**
     * Resolves MCP configuration by config ID and extracts integration details.
     */
    public McpConfigurationResult resolveMcpConfiguration(String configId) throws Exception {
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

        logConfigurationResolution(configId, integrationIds, userId, configuredIntegrationTypes);

        if (configuredIntegrationTypes.isEmpty()) {
            throw new IllegalArgumentException("No valid integrations found for configId " + configId);
        }

        return new McpConfigurationResult(mcpConfig, userId, integrationIds, configuredIntegrationTypes);
    }

    /**
     * Generates tools list based on MCP configuration.
     */
    public McpToolsListResponse generateToolsList(String configId) throws Exception {
        McpConfigurationResult result = resolveMcpConfiguration(configId);
        
        // Generate actual tools based on configured integrations only
        Map<String, Object> toolsList = MCPSchemaGenerator.generateToolsListResponse(result.getIntegrationTypes());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> toolsFromGenerator = (List<Map<String, Object>>) toolsList.get("tools");

        return new McpToolsListResponse(toolsFromGenerator);
    }

    /**
     * Processes tool call with MCP configuration resolution.
     */
    public McpToolCallResponse processToolCall(String configId, String toolName, 
                                             JSONObject arguments, HttpServletRequest request) throws Exception {
        // Validate tool name
        if (toolName == null || toolName.isEmpty()) {
            throw new IllegalArgumentException("missing tool name");
        }

        McpConfigurationResult configResult = resolveMcpConfiguration(configId);

        // Execute the tool via the existing MCP tool execution logic - returns raw Object result
        Object result = executeToolCallRaw(configResult, toolName, arguments);
        
        // Handle File results with proper JSON response
        if (result instanceof File) {
            File file = (File) result;
            String downloadToken = fileDownloadService.createDownloadToken(file);
            
            // Build full URL using request context
            String downloadUrl = buildDownloadUrl(request, downloadToken);
            
            String mimeType = determineMimeType(file);
            return McpToolCallResponse.file(downloadUrl, file.getName(), mimeType, "15 minutes");
        }
        
        // Handle regular string results
        String textResult = (result != null) ? result.toString() : "Tool executed successfully but returned no result.";
        return McpToolCallResponse.text(textResult);
    }

    /**
     * Gets available tools for a given MCP configuration as a Map.
     * Useful for ChatService integration.
     */
    public Map<String, Object> getToolsListAsMap(String configId) throws Exception {
        McpConfigurationResult result = resolveMcpConfiguration(configId);
        return MCPSchemaGenerator.generateToolsListResponse(result.getIntegrationTypes());
    }
    
    /**
     * Gets available tools for a given MCP configuration result as a Map.
     * More efficient when configuration has already been resolved.
     */
    public Map<String, Object> getToolsListAsMap(McpConfigurationResult configResult) throws Exception {
        return MCPSchemaGenerator.generateToolsListResponse(configResult.getIntegrationTypes());
    }
    
    /**
     * Resolves MCP configuration for a given config ID.
     * Useful when you need to resolve once and use multiple times.
     */
    public McpConfigurationResult resolveMcpConfigurationResult(String configId) throws Exception {
        return resolveMcpConfiguration(configId);
    }

    /**
     * Executes a tool call and returns the raw result.
     * Suitable for integration with ChatService without HTTP request context.
     */
    public Object executeToolCallRaw(String configId, String toolName, Map<String, Object> arguments) throws Exception {
        McpConfigurationResult configResult = resolveMcpConfiguration(configId);
        
        // Convert Map to JSONObject for compatibility
        JSONObject jsonArguments = new JSONObject();
        if (arguments != null) {
            arguments.forEach(jsonArguments::put);
        }
        
        return executeToolCallRaw(configResult, toolName, jsonArguments);
    }
    
    /**
     * Executes a tool call with already resolved configuration and returns the raw result.
     * More efficient when configuration has already been resolved.
     */
    public Object executeToolCallRaw(McpConfigurationResult configResult, String toolName, Map<String, Object> arguments) throws Exception {
        // Convert Map to JSONObject for compatibility
        JSONObject jsonArguments = new JSONObject();
        if (arguments != null) {
            arguments.forEach(jsonArguments::put);
        }
        
        return executeToolCallRaw(configResult, toolName, jsonArguments);
    }

    /**
     * Internal method to execute tool calls with configuration result.
     */
    private Object executeToolCallRaw(McpConfigurationResult configResult, String toolName, JSONObject arguments) throws Exception {
        try {
            String userId = configResult.getUserId();
            List<String> integrationIds = configResult.getIntegrationIds();
            
            logger.info("Executing tool {} for integrations: {}", toolName, configResult.getIntegrationTypes());
            
            // Create client instances
            Map<String, Object> clientInstances = createClientInstances(userId, integrationIds);
            
            // Convert JSONObject arguments to Map with proper type conversion
            Map<String, Object> argumentsMap = new HashMap<>();
            if (arguments != null) {
                arguments.keys().forEachRemaining(key -> {
                    Object value = arguments.get(key);
                    // Convert ArrayList to String[] if needed (common issue with MCP tools)
                    if (value instanceof ArrayList) {
                        ArrayList<?> list = (ArrayList<?>) value;
                        String[] array = list.stream()
                            .map(Object::toString)
                            .toArray(String[]::new);
                        logger.debug("Converted ArrayList parameter '{}' to String[{}] for tool {}", 
                                   key, array.length, toolName);
                        argumentsMap.put(key, array);
                    } else {
                        argumentsMap.put(key, value);
                    }
                });
            }

            // Execute the tool
            return MCPToolExecutor.executeTool(toolName, argumentsMap, clientInstances);
            
        } catch (Exception e) {
            logger.error("Error in executeToolCallRaw", e);
            throw new RuntimeException("Failed to execute tool: " + e.getMessage(), e);
        }
    }

    /**
     * Creates client instances for the given user and integration IDs.
     */
    public Map<String, Object> createClientInstances(String userId, List<String> integrationIds) throws Exception {
        if (userId == null || integrationIds == null) {
            throw new IllegalArgumentException("User ID or integration IDs not available");
        }

        logger.info("Creating client instances for user {} with integrations: {}", userId, integrationIds);

        // Resolve integrations from IDs
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

    /**
     * Builds download URL from HTTP request context.
     */
    private String buildDownloadUrl(HttpServletRequest request, String downloadToken) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();
        
        if ((scheme.equals("http") && serverPort == 80) || (scheme.equals("https") && serverPort == 443)) {
            return String.format("%s://%s%s/api/files/download/%s", scheme, serverName, contextPath, downloadToken);
        } else {
            return String.format("%s://%s:%d%s/api/files/download/%s", scheme, serverName, serverPort, contextPath, downloadToken);
        }
    }

    /**
     * Determines MIME type based on file extension.
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
     * Logs configuration resolution details.
     */
    private void logConfigurationResolution(String configId, List<String> integrationIds, 
                                          String userId, Set<String> types) {
        if (logger.isInfoEnabled()) {
            logger.info("Configuration {} resolved: {} integrations for user {}, types: {}", 
                configId, integrationIds.size(), userId, types);
        }
    }
}
