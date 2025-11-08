package com.github.istin.dmtools.server;

import com.github.istin.dmtools.common.utils.PropertyReader;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Service responsible for resolving integration credentials for server-managed job execution.
 * This service handles the secure resolution of integration configurations and credentials
 * before they are passed to the dmtools-core for job execution.
 */
@Service
public class IntegrationResolutionService {
    
    private static final Logger logger = LogManager.getLogger(IntegrationResolutionService.class);

    @Autowired
    private PropertyReader propertyReader;

    /**
     * Resolves integrations required for a specific job.
     * This method retrieves and decrypts credentials for the requested integrations.
     * 
     * @param jobName The name of the job requiring integrations
     * @param requiredIntegrations List of integration types needed (e.g., "jira", "confluence", "dial")
     * @return JSONObject containing resolved integration configurations
     */
    public JSONObject resolveIntegrationsForJob(String jobName, List<String> requiredIntegrations) {
        logger.info("🔧 [IntegrationResolutionService] Resolving integrations for job '{}': {}", jobName, requiredIntegrations);
        
        // Check if we're dealing with integration IDs vs integration types
        boolean hasIntegrationIds = requiredIntegrations.stream()
            .anyMatch(id -> id.contains("-") && id.length() > 10); // UUIDs contain dashes and are longer
        
        if (hasIntegrationIds) {
            logger.warn("⚠️ [IntegrationResolutionService] Detected integration IDs instead of types: {}. This service only handles integration types (jira, ai, etc.), not database IDs!", requiredIntegrations);
        }
        
        JSONObject resolved = new JSONObject();
        
        for (String integrationType : requiredIntegrations) {
            try {
                logger.debug("🔍 [IntegrationResolutionService] Attempting to resolve integration type: '{}'", integrationType);
                JSONObject credentials = resolveIntegration(integrationType);
                if (credentials != null && credentials.length() > 0) {
                    resolved.put(integrationType, credentials);
                    logger.info("✅ [IntegrationResolutionService] Successfully resolved integration type: '{}'", integrationType);
                } else {
                    logger.warn("🔴 [IntegrationResolutionService] No credentials found for integration type: '{}'", integrationType);
                }
            } catch (Exception e) {
                logger.error("❌ [IntegrationResolutionService] Failed to resolve integration type '{}': {}", integrationType, e.getMessage(), e);
                // Continue with other integrations even if one fails
            }
        }
        
        logger.info("📊 [IntegrationResolutionService] Resolved {} out of {} integrations for job '{}'", 
                   resolved.length(), requiredIntegrations.size(), jobName);
        
        return resolved;
    }
    
    /**
     * Resolves a specific integration type.
     * 
     * @param integrationType The type of integration to resolve (jira, confluence, dial, etc.)
     * @return JSONObject containing the integration configuration
     */
    private JSONObject resolveIntegration(String integrationType) {
        JSONObject credentials = new JSONObject();
        
        switch (integrationType.toLowerCase()) {
            // Legacy direct names (maintained for backward compatibility)
            case "tracker":
            case "jira":
                return resolveJiraIntegration();
            case "wiki":
            case "confluence":
                return resolveConfluenceIntegration();
            case "ai":
            case "dial":
                return resolveDialIntegration();
            case "ollama":
                return resolveOllamaIntegration();
            case "github":
                return resolveGitHubIntegration();
                
            // New category-based names
            case "trackerclient":
                // TrackerClient category maps to Jira by default
                return resolveJiraIntegration();
            case "documentation":
                // Documentation category maps to Confluence by default
                return resolveConfluenceIntegration();
            case "sourcecode":
                // SourceCode category maps to GitHub by default
                return resolveGitHubIntegration();
                
            default:
                logger.warn("Unknown integration type: {}", integrationType);
                return credentials;
        }
    }
    
    /**
     * Resolves JIRA integration credentials.
     */
    private JSONObject resolveJiraIntegration() {
        JSONObject jiraConfig = new JSONObject();
        
        String jiraUrl = propertyReader.getJiraBasePath();
        String jiraToken = propertyReader.getJiraLoginPassToken();
        String jiraAuthType = propertyReader.getJiraAuthType();
        
        if (jiraUrl != null && !jiraUrl.trim().isEmpty()) {
            jiraConfig.put("url", jiraUrl);
        }
        if (jiraToken != null && !jiraToken.trim().isEmpty()) {
            jiraConfig.put("token", jiraToken);
        }
        if (jiraAuthType != null && !jiraAuthType.trim().isEmpty()) {
            jiraConfig.put("authType", jiraAuthType);
        }
        
        return jiraConfig;
    }
    
    /**
     * Resolves Confluence integration credentials.
     */
    private JSONObject resolveConfluenceIntegration() {
        JSONObject confluenceConfig = new JSONObject();
        
        String confluenceUrl = propertyReader.getConfluenceBasePath();
        String confluenceToken = propertyReader.getConfluenceLoginPassToken();
        String confluenceAuthType = propertyReader.getConfluenceAuthType();
        String confluenceSpace = propertyReader.getConfluenceDefaultSpace();
        
        if (confluenceUrl != null && !confluenceUrl.trim().isEmpty()) {
            confluenceConfig.put("url", confluenceUrl);
        }
        if (confluenceToken != null && !confluenceToken.trim().isEmpty()) {
            confluenceConfig.put("token", confluenceToken);
        }
        if (confluenceAuthType != null && !confluenceAuthType.trim().isEmpty()) {
            confluenceConfig.put("authType", confluenceAuthType);
        }
        if (confluenceSpace != null && !confluenceSpace.trim().isEmpty()) {
            confluenceConfig.put("defaultSpace", confluenceSpace);
        }
        
        return confluenceConfig;
    }
    
    /**
     * Resolves Dial integration credentials.
     */
    private JSONObject resolveDialIntegration() {
        JSONObject dialConfig = new JSONObject();
        
        String apiKey = propertyReader.getDialIApiKey();
        String model = propertyReader.getDialModel();
        String basePath = propertyReader.getDialBathPath();
        
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            dialConfig.put("apiKey", apiKey);
        }
        if (model != null && !model.trim().isEmpty()) {
            dialConfig.put("model", model);
        }
        if (basePath != null && !basePath.trim().isEmpty()) {
            dialConfig.put("basePath", basePath);
        }
        
        return dialConfig;
    }
    
    /**
     * Resolves Ollama integration credentials.
     */
    private JSONObject resolveOllamaIntegration() {
        JSONObject ollamaConfig = new JSONObject();
        
        String basePath = propertyReader.getOllamaBasePath();
        String model = propertyReader.getOllamaModel();
        int numCtx = propertyReader.getOllamaNumCtx();
        int numPredict = propertyReader.getOllamaNumPredict();
        
        if (basePath != null && !basePath.trim().isEmpty()) {
            ollamaConfig.put("OLLAMA_BASE_PATH", basePath);
        }
        if (model != null && !model.trim().isEmpty()) {
            ollamaConfig.put("OLLAMA_MODEL", model);
        }
        ollamaConfig.put("OLLAMA_NUM_CTX", numCtx);
        ollamaConfig.put("OLLAMA_NUM_PREDICT", numPredict);
        
        return ollamaConfig;
    }
    
    /**
     * Resolves GitHub integration credentials.
     */
    private JSONObject resolveGitHubIntegration() {
        JSONObject githubConfig = new JSONObject();
        
        // Add GitHub-specific credential resolution logic here
        // This would typically read from stored configuration or environment variables
        
        return githubConfig;
    }
    
    /**
     * Determines the required integrations for a specific job type.
     * 
     * @param jobName The name of the job
     * @return List of required integration types
     */
    public List<String> getRequiredIntegrationsForJob(String jobName) {
        // Define job-specific integration requirements
        switch (jobName.toLowerCase()) {
            case "expert":
                return List.of("tracker", "wiki", "ai");
            case "testcasesgenerator":
                return List.of("tracker", "wiki", "ai");
            default:
                // Default set of integrations for unknown jobs
                return List.of("tracker", "ai");
        }
    }
} 