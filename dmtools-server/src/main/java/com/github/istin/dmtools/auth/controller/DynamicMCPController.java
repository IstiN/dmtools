package com.github.istin.dmtools.auth.controller;

import com.github.istin.dmtools.auth.service.UserService;
import com.github.istin.dmtools.auth.service.IntegrationService;
import com.github.istin.dmtools.di.ServerManagedIntegrationsModule;
import com.github.istin.dmtools.dto.IntegrationConfigDto;
import com.github.istin.dmtools.dto.IntegrationDto;
import com.github.istin.dmtools.mcp.generated.MCPSchemaGenerator;
import com.github.istin.dmtools.mcp.generated.MCPToolExecutor;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Dynamic MCP controller that handles MCP protocol requests using generated tool registry and executor.
 * Uses ServerManagedIntegrationsModule for integration client instantiation.
 */
@RestController
@RequestMapping("/mcp")
public class DynamicMCPController {

    private static final Logger logger = LoggerFactory.getLogger(DynamicMCPController.class);

    private final UserService userService;
    private final IntegrationService integrationService;

    @Autowired
    public DynamicMCPController(
            UserService userService,
            IntegrationService integrationService) {
        this.userService = userService;
        this.integrationService = integrationService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> handleMcpRequest(
            @RequestBody Map<String, Object> request,
            @RequestParam(required = false) String userId) {
        
        try {
            String method = (String) request.get("method");
            if (method == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("Missing method parameter"));
            }

            String effectiveUserId = extractUserId(request, userId);
            if (effectiveUserId == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("Missing or invalid user identification"));
            }

            if (userService.findById(effectiveUserId).isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("User not found: " + effectiveUserId));
            }

            return switch (method) {
                case "tools/list" -> ResponseEntity.ok(handleToolsList(effectiveUserId));
                case "tools/call" -> ResponseEntity.ok(handleToolCall((Map<String, Object>) request.get("params"), effectiveUserId));
                default -> ResponseEntity.badRequest().body(createErrorResponse("Unsupported method: " + method));
            };

        } catch (Exception e) {
            logger.error("Error handling MCP request", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("Internal server error: " + e.getMessage()));
        }
    }

    private Map<String, Object> handleToolsList(String userId) {
        try {
            Set<String> userIntegrations = getUserIntegrationTypes(userId);
            
            if (userIntegrations.isEmpty()) {
                logger.info("User {} has no integrations configured", userId);
                return Map.of("tools", new Object[0]);
            }
            MCPSchemaGenerator schemaGenerator = new MCPSchemaGenerator();
            return schemaGenerator.generateToolsListResponse(userIntegrations);
        } catch (Exception e) {
            logger.error("Error generating tools list for user {}", userId, e);
            return createErrorResponse("Error generating tools list: " + e.getMessage());
        }
    }

    private Map<String, Object> handleToolCall(Map<String, Object> params, String userId) {
        try {
            if (params == null) {
                return createErrorResponse("Missing tool call parameters");
            }

            String toolName = (String) params.get("name");
            Map<String, Object> arguments = (Map<String, Object>) params.get("arguments");

            if (toolName == null) {
                return createErrorResponse("Missing tool name");
            }

            Map<String, Object> clientInstances = getClientInstancesUsingModule(userId);
            if (clientInstances.isEmpty()) {
                return createErrorResponse("No client instances available for user");
            }

            Object result = MCPToolExecutor.executeTool(toolName, arguments != null ? arguments : new HashMap<>(), clientInstances);
            return formatMCPResult(result);
        } catch (Exception e) {
            logger.error("Error executing tool call for user {}", userId, e);
            return formatMCPError(e);
        }
    }

    private Map<String, Object> getClientInstancesUsingModule(String userId) {
        JSONObject resolvedIntegrations = buildResolvedIntegrations(userId);
        
        if (resolvedIntegrations.length() == 0) {
            logger.warn("No resolved integrations for user {}", userId);
            return new HashMap<>();
        }

        ServerManagedIntegrationsModule module = new ServerManagedIntegrationsModule(resolvedIntegrations);
        
        Map<String, Object> clientInstances = new HashMap<>();
        
        if (resolvedIntegrations.has("jira")) {
            clientInstances.put("jira", module.provideTrackerClient());
        }
        
        if (resolvedIntegrations.has("confluence")) {
            clientInstances.put("confluence", module.provideConfluence());
        }
        
        return clientInstances;
    }

    private JSONObject buildResolvedIntegrations(String userId) {
        JSONObject resolvedIntegrations = new JSONObject();
        List<IntegrationDto> integrations = integrationService.getIntegrationsForUser(userId);

        for (IntegrationDto integrationStub : integrations) {
            IntegrationDto integration = integrationService.getIntegrationById(integrationStub.getId(), userId, true);
            
            Map<String, String> configMap = integration.getConfigParams().stream()
                    .filter(config -> config.getParamValue() != null)
                    .collect(Collectors.toMap(IntegrationConfigDto::getParamKey, IntegrationConfigDto::getParamValue));

            JSONObject integrationJson = new JSONObject();
            if ("jira".equals(integration.getType())) {
                integrationJson.put("url", configMap.get("JIRA_BASE_PATH"));
                integrationJson.put("token", configMap.get("JIRA_LOGIN_PASS_TOKEN"));
                integrationJson.put("authType", configMap.get("JIRA_AUTH_TYPE"));
                resolvedIntegrations.put("jira", integrationJson);
            } else if ("confluence".equals(integration.getType())) {
                integrationJson.put("url", configMap.get("CONFLUENCE_BASE_PATH"));
                integrationJson.put("token", configMap.get("CONFLUENCE_API_TOKEN"));
                integrationJson.put("defaultSpace", configMap.get("CONFLUENCE_DEFAULT_SPACE"));
                resolvedIntegrations.put("confluence", integrationJson);
            }
        }
        return resolvedIntegrations;
    }

    private String extractUserId(Map<String, Object> request, String paramUserId) {
        if (paramUserId != null && !paramUserId.trim().isEmpty()) {
            return paramUserId;
        }

        Map<String, Object> meta = (Map<String, Object>) request.get("meta");
        if (meta != null) {
            Object progressToken = meta.get("progressToken");
            if (progressToken instanceof String) {
                return extractUserFromProgressToken((String) progressToken);
            }
        }
        return null;
    }

    private String extractUserFromProgressToken(String progressToken) {
        return progressToken;
    }

    private Map<String, Object> formatMCPResult(Object result) {
        Map<String, Object> response = new HashMap<>();
        response.put("content", Map.of(
            "type", "text",
            "text", result != null ? result.toString() : "Success"
        ));
        response.put("isError", false);
        return response;
    }

    private Map<String, Object> formatMCPError(Exception e) {
        Map<String, Object> response = new HashMap<>();
        response.put("content", Map.of(
            "type", "text",
            "text", "Error: " + e.getMessage()
        ));
        response.put("isError", true);
        return response;
    }

    private Map<String, Object> createErrorResponse(String message) {
        return Map.of("error", Map.of("code", -1, "message", message));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "ok");
        status.put("service", "Dynamic MCP Controller");
        status.put("generatedClasses", "available");
        return ResponseEntity.ok(status);
    }

    private Set<String> getUserIntegrationTypes(String userId) {
        return integrationService.getIntegrationsForUser(userId).stream()
                .map(IntegrationDto::getType)
                .collect(Collectors.toSet());
    }

    private boolean userHasMcpConfigurations(String userId) {
        return integrationService.getIntegrationsForUser(userId).stream()
                .anyMatch(integration -> "jira".equals(integration.getType()) || "confluence".equals(integration.getType()));
    }

    @GetMapping("/tools/{userId}")
    public ResponseEntity<Map<String, Object>> getUserTools(@PathVariable String userId) {
        try {
            if (userService.findById(userId).isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("User not found: " + userId));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("integrationTypes", getUserIntegrationTypes(userId));
            response.put("hasConfigurations", userHasMcpConfigurations(userId));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting tools for user {}", userId, e);
            return ResponseEntity.internalServerError().body(createErrorResponse("Error: " + e.getMessage()));
        }
    }
} 