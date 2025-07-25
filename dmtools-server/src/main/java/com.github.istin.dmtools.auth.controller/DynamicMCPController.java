package com.github.istin.dmtools.auth.controller;

import com.github.istin.dmtools.auth.service.UserService;
import com.github.istin.dmtools.auth.service.IntegrationService;
import com.github.istin.dmtools.di.ServerManagedIntegrationsModule;
import com.github.istin.dmtools.dto.IntegrationConfigDto;
import com.github.istin.dmtools.dto.IntegrationDto;
// import com.github.istin.dmtools.mcp.generated.MCPSchemaGenerator;
// import com.github.istin.dmtools.mcp.generated.MCPToolExecutor;
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
//... existing code ...
    private Map<String, Object> handleToolsList(String userId) {
        try {
            Set<String> userIntegrations = getUserIntegrationTypes(userId);

            if (userIntegrations.isEmpty()) {
                logger.info("User {} has no integrations configured", userId);
                return Map.of("tools", new Object[0]);
            }
            // MCPSchemaGenerator schemaGenerator = new MCPSchemaGenerator();
            // return schemaGenerator.generateToolsListResponse(userIntegrations);
            return new HashMap<>(); // Placeholder
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

            // Object result = MCPToolExecutor.executeTool(toolName, arguments != null ? arguments : new HashMap<>(), clientInstances);
            // return formatMCPResult(result);
            return new HashMap<>(); // Placeholder
        } catch (Exception e) {
            logger.error("Error executing tool call for user {}", userId, e);
            return formatMCPError(e);
        }
    }
//... existing code ...
} 