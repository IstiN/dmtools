package com.github.istin.dmtools.server.util;

import com.github.istin.dmtools.dto.IntegrationDto;
import com.github.istin.dmtools.dto.IntegrationConfigDto;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for mapping integration configuration from database format to ServerManagedIntegrationsModule format.
 * This class contains the common logic used by both JobExecutionController and DynamicMCPController.
 */
public class IntegrationConfigMapper {
    
    private static final Logger logger = LoggerFactory.getLogger(IntegrationConfigMapper.class);
    
    /**
     * Maps integration configuration from database format to ServerManagedIntegrationsModule format.
     * 
     * @param integrationDto The integration configuration from database
     * @return JSONObject in the format expected by ServerManagedIntegrationsModule
     */
    public static JSONObject mapIntegrationConfig(IntegrationDto integrationDto) {
        JSONObject config = new JSONObject();
        
        // Create a map of config parameters for easy lookup
        Map<String, String> params = new HashMap<>();
        if (integrationDto.getConfigParams() != null) {
            for (IntegrationConfigDto param : integrationDto.getConfigParams()) {
                params.put(param.getParamKey(), param.getParamValue());
            }
        }
        
        logger.info("🔧 Mapping integration config for type '{}' with parameters: {}", 
            integrationDto.getType(), params.keySet());
        
        // Map based on integration type
        switch (integrationDto.getType().toLowerCase()) {
            case "tracker":
            case "jira":
                mapJiraIntegration(config, params);
                break;
                
            case "wiki":
            case "confluence":
                mapConfluenceIntegration(config, params);
                break;
                
            case "ai":
            case "dial":
                mapDialIntegration(config, params);
                break;
                
            case "gemini":
                mapGeminiIntegration(config, params);
                break;
                
            case "figma":
                mapFigmaIntegration(config, params);
                break;
                
            default:
                logger.warn("Unknown integration type: {}", integrationDto.getType());
                break;
        }
        
        return config;
    }
    
    private static void mapJiraIntegration(JSONObject config, Map<String, String> params) {
        logger.info("🔍 Processing JIRA integration mapping...");
        
        // Map database parameters to expected JIRA format
        if (params.containsKey("url")) {
            config.put("url", params.get("url"));
            logger.info("  ✅ Mapped 'url' parameter: {}", params.get("url"));
        } else if (params.containsKey("basePath")) {
            config.put("url", params.get("basePath"));
            logger.info("  ✅ Mapped 'basePath' to 'url': {}", params.get("basePath"));
        } else if (params.containsKey("baseUrl")) {
            config.put("url", params.get("baseUrl"));
            logger.info("  ✅ Mapped 'baseUrl' to 'url': {}", params.get("baseUrl"));
        } else if (params.containsKey("JIRA_BASE_PATH")) {
            config.put("url", params.get("JIRA_BASE_PATH"));
            logger.info("  ✅ Mapped 'JIRA_BASE_PATH' to 'url': {}", params.get("JIRA_BASE_PATH"));
        } else {
            logger.warn("  ⚠️  No URL parameter found for JIRA integration");
        }
        
        // Priority 1: Use separate email and API token if both are available
        if (params.containsKey("JIRA_EMAIL") && params.containsKey("JIRA_API_TOKEN")) {
            String email = params.get("JIRA_EMAIL");
            String apiToken = params.get("JIRA_API_TOKEN");
            if (email != null && !email.trim().isEmpty() && 
                apiToken != null && !apiToken.trim().isEmpty()) {
                // Automatically combine email:token and base64 encode
                String credentials = email.trim() + ":" + apiToken.trim();
                String encodedToken = java.util.Base64.getEncoder().encodeToString(credentials.getBytes());
                config.put("token", encodedToken);
                logger.info("  ✅ Mapped 'JIRA_EMAIL' + 'JIRA_API_TOKEN' to auto-encoded token: [SENSITIVE]");
            }
        } 
        // Priority 2: Use legacy token methods
        else if (params.containsKey("token")) {
            config.put("token", params.get("token"));
            logger.info("  ✅ Mapped 'token' parameter: [SENSITIVE]");
        } else if (params.containsKey("password")) {
            config.put("token", params.get("password"));
            logger.info("  ✅ Mapped 'password' to 'token': [SENSITIVE]");
        } else if (params.containsKey("JIRA_LOGIN_PASS_TOKEN")) {
            config.put("token", params.get("JIRA_LOGIN_PASS_TOKEN"));
            logger.info("  ✅ Mapped 'JIRA_LOGIN_PASS_TOKEN' to 'token': [SENSITIVE]");
        } else {
            logger.warn("  ⚠️  No authentication parameters found for JIRA integration");
        }
        
        if (params.containsKey("authType")) {
            config.put("authType", params.get("authType"));
            logger.info("  ✅ Mapped 'authType' parameter: {}", params.get("authType"));
        } else if (params.containsKey("JIRA_AUTH_TYPE")) {
            config.put("authType", params.get("JIRA_AUTH_TYPE"));
            logger.info("  ✅ Mapped 'JIRA_AUTH_TYPE' to 'authType': {}", params.get("JIRA_AUTH_TYPE"));
        } else {
            config.put("authType", "token"); // Default to token auth
            logger.info("  ✅ Set default 'authType': token");
        }
        
        if (params.containsKey("username")) {
            config.put("username", params.get("username"));
            logger.info("  ✅ Mapped 'username' parameter: {}", params.get("username"));
        } else if (params.containsKey("JIRA_USERNAME")) {
            config.put("username", params.get("JIRA_USERNAME"));
            logger.info("  ✅ Mapped 'JIRA_USERNAME' to 'username': {}", params.get("JIRA_USERNAME"));
        }
    }
    
    private static void mapConfluenceIntegration(JSONObject config, Map<String, String> params) {
        logger.info("🔍 Processing Confluence integration mapping...");
        
        if (params.containsKey("url")) {
            config.put("url", params.get("url"));
            logger.info("  ✅ Mapped 'url' parameter: {}", params.get("url"));
        } else if (params.containsKey("basePath")) {
            config.put("url", params.get("basePath"));
            logger.info("  ✅ Mapped 'basePath' to 'url': {}", params.get("basePath"));
        } else if (params.containsKey("CONFLUENCE_BASE_PATH")) {
            config.put("url", params.get("CONFLUENCE_BASE_PATH"));
            logger.info("  ✅ Mapped 'CONFLUENCE_BASE_PATH' to 'url': {}", params.get("CONFLUENCE_BASE_PATH"));
        } else {
            logger.warn("  ⚠️  No URL parameter found for Confluence integration");
        }
        
        // Priority 1: Use separate email and API token if both are available
        if (params.containsKey("CONFLUENCE_EMAIL") && params.containsKey("CONFLUENCE_API_TOKEN")) {
            String email = params.get("CONFLUENCE_EMAIL");
            String apiToken = params.get("CONFLUENCE_API_TOKEN");
            String authType = params.getOrDefault("CONFLUENCE_AUTH_TYPE", "Basic");
            
            if (email != null && !email.trim().isEmpty() && 
                apiToken != null && !apiToken.trim().isEmpty()) {
                
                // For Bearer auth, use token directly without email combination
                if ("Bearer".equalsIgnoreCase(authType)) {
                    config.put("token", apiToken.trim());
                    config.put("authType", "Bearer");
                    logger.info("  ✅ Mapped 'CONFLUENCE_EMAIL' + 'CONFLUENCE_API_TOKEN' to Bearer token: [SENSITIVE]");
                } else {
                    // For Basic auth (default), combine email:token and base64 encode
                    String credentials = email.trim() + ":" + apiToken.trim();
                    String encodedToken = java.util.Base64.getEncoder().encodeToString(credentials.getBytes());
                    config.put("token", encodedToken);
                    config.put("authType", "Basic");
                    logger.info("  ✅ Mapped 'CONFLUENCE_EMAIL' + 'CONFLUENCE_API_TOKEN' to auto-encoded Basic token: [SENSITIVE]");
                }
            }
        } 
        // Priority 2: Use legacy token methods
        else if (params.containsKey("token")) {
            config.put("token", params.get("token"));
            logger.info("  ✅ Mapped 'token' parameter: [SENSITIVE]");
        } else if (params.containsKey("CONFLUENCE_LOGIN_PASS_TOKEN")) {
            config.put("token", params.get("CONFLUENCE_LOGIN_PASS_TOKEN"));
            logger.info("  ✅ Mapped 'CONFLUENCE_LOGIN_PASS_TOKEN' to 'token': [SENSITIVE]");
        } else {
            logger.warn("  ⚠️  No authentication parameters found for Confluence integration");
        }
        
        // Handle auth type if provided separately
        if (params.containsKey("CONFLUENCE_AUTH_TYPE") && !config.has("authType")) {
            config.put("authType", params.get("CONFLUENCE_AUTH_TYPE"));
            logger.info("  ✅ Mapped 'CONFLUENCE_AUTH_TYPE' to 'authType': {}", params.get("CONFLUENCE_AUTH_TYPE"));
        }
        
        if (params.containsKey("defaultSpace")) {
            config.put("defaultSpace", params.get("defaultSpace"));
            logger.info("  ✅ Mapped 'defaultSpace' parameter: {}", params.get("defaultSpace"));
        } else if (params.containsKey("space")) {
            config.put("defaultSpace", params.get("space"));
            logger.info("  ✅ Mapped 'space' to 'defaultSpace': {}", params.get("space"));
        } else if (params.containsKey("CONFLUENCE_DEFAULT_SPACE")) {
            config.put("defaultSpace", params.get("CONFLUENCE_DEFAULT_SPACE"));
            logger.info("  ✅ Mapped 'CONFLUENCE_DEFAULT_SPACE' to 'defaultSpace': {}", params.get("CONFLUENCE_DEFAULT_SPACE"));
        } else {
            logger.warn("  ⚠️  No defaultSpace parameter found for Confluence integration");
        }
    }
    
    private static void mapDialIntegration(JSONObject config, Map<String, String> params) {
        logger.info("🔍 Processing Dial integration mapping...");
        
        if (params.containsKey("apiKey")) {
            config.put("apiKey", params.get("apiKey"));
            logger.info("  ✅ Mapped 'apiKey' parameter: [SENSITIVE]");
        } else {
            logger.warn("  ⚠️  No apiKey parameter found for OpenAI integration");
        }
        
        if (params.containsKey("model")) {
            config.put("model", params.get("model"));
            logger.info("  ✅ Mapped 'model' parameter: {}", params.get("model"));
        }
        
        if (params.containsKey("basePath")) {
            config.put("basePath", params.get("basePath"));
            logger.info("  ✅ Mapped 'basePath' parameter: {}", params.get("basePath"));
        }
    }
    
    private static void mapGeminiIntegration(JSONObject config, Map<String, String> params) {
        logger.info("🔍 Processing Gemini integration mapping...");
        
        if (params.containsKey("apiKey")) {
            config.put("apiKey", params.get("apiKey"));
            logger.info("  ✅ Mapped 'apiKey' parameter: [SENSITIVE]");
        } else if (params.containsKey("GEMINI_API_KEY")) {
            config.put("apiKey", params.get("GEMINI_API_KEY"));
            logger.info("  ✅ Mapped 'GEMINI_API_KEY' to 'apiKey': [SENSITIVE]");
        } else {
            logger.warn("  ⚠️  No apiKey parameter found for Gemini integration");
        }
        
        if (params.containsKey("model")) {
            config.put("model", params.get("model"));
            logger.info("  ✅ Mapped 'model' parameter: {}", params.get("model"));
        } else if (params.containsKey("GEMINI_DEFAULT_MODEL")) {
            config.put("model", params.get("GEMINI_DEFAULT_MODEL"));
            logger.info("  ✅ Mapped 'GEMINI_DEFAULT_MODEL' to 'model': {}", params.get("GEMINI_DEFAULT_MODEL"));
        }
        
        if (params.containsKey("basePath")) {
            config.put("basePath", params.get("basePath"));
            logger.info("  ✅ Mapped 'basePath' parameter: {}", params.get("basePath"));
        } else if (params.containsKey("GEMINI_BASE_PATH")) {
            config.put("basePath", params.get("GEMINI_BASE_PATH"));
            logger.info("  ✅ Mapped 'GEMINI_BASE_PATH' to 'basePath': {}", params.get("GEMINI_BASE_PATH"));
        }
    }

    private static void mapFigmaIntegration(JSONObject config, Map<String, String> params) {
        logger.info("🔍 Processing Figma integration mapping...");
        
        // Map Figma base path
        if (params.containsKey("FIGMA_BASE_PATH")) {
            config.put("FIGMA_BASE_PATH", params.get("FIGMA_BASE_PATH"));
            logger.info("  ✅ Mapped 'FIGMA_BASE_PATH' parameter: {}", params.get("FIGMA_BASE_PATH"));
        } else if (params.containsKey("basePath")) {
            config.put("FIGMA_BASE_PATH", params.get("basePath"));
            logger.info("  ✅ Mapped 'basePath' to 'FIGMA_BASE_PATH': {}", params.get("basePath"));
        } else if (params.containsKey("url")) {
            config.put("FIGMA_BASE_PATH", params.get("url"));
            logger.info("  ✅ Mapped 'url' to 'FIGMA_BASE_PATH': {}", params.get("url"));
        } else {
            // Set default Figma API base path
            config.put("FIGMA_BASE_PATH", "https://api.figma.com");
            logger.info("  ✅ Set default 'FIGMA_BASE_PATH': https://api.figma.com");
        }
        
        // Map Figma token
        if (params.containsKey("FIGMA_TOKEN")) {
            config.put("FIGMA_TOKEN", params.get("FIGMA_TOKEN"));
            logger.info("  ✅ Mapped 'FIGMA_TOKEN' parameter: [SENSITIVE]");
        } else if (params.containsKey("token")) {
            config.put("FIGMA_TOKEN", params.get("token"));
            logger.info("  ✅ Mapped 'token' to 'FIGMA_TOKEN': [SENSITIVE]");
        } else if (params.containsKey("apiKey")) {
            config.put("FIGMA_TOKEN", params.get("apiKey"));
            logger.info("  ✅ Mapped 'apiKey' to 'FIGMA_TOKEN': [SENSITIVE]");
        } else if (params.containsKey("FIGMA_API_KEY")) {
            config.put("FIGMA_TOKEN", params.get("FIGMA_API_KEY"));
            logger.info("  ✅ Mapped 'FIGMA_API_KEY' to 'FIGMA_TOKEN': [SENSITIVE]");
        } else {
            logger.warn("  ⚠️  No token parameter found for Figma integration");
        }
    }
} 