package com.github.istin.dmtools.server.service;

import com.github.istin.dmtools.auth.service.IntegrationService;
import com.github.istin.dmtools.auth.service.IntegrationConfigurationLoader;
import com.github.istin.dmtools.server.util.IntegrationConfigMapper;
import com.github.istin.dmtools.dto.IntegrationDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * Shared service for resolving integration IDs to configuration objects.
 * Extracted from JobExecutionController and DynamicMCPController to provide
 * consistent integration resolution logic across the application.
 */
@Service
public class IntegrationResolutionHelper {

    private static final Logger logger = LogManager.getLogger(IntegrationResolutionHelper.class);

    @Autowired
    private IntegrationService integrationService;

    @Autowired
    private IntegrationConfigurationLoader configurationLoader;

    /**
     * Resolves integration IDs to JSONObject configuration.
     * 
     * @param integrationIds List of integration IDs to resolve
     * @param userId The user ID for access control
     * @return JSONObject containing resolved integration configurations
     */
    public JSONObject resolveIntegrationIds(List<String> integrationIds, String userId) {
        JSONObject resolved = new JSONObject();
        
        for (String integrationId : integrationIds) {
            try {
                logger.info("🔍 [IntegrationResolutionHelper] Resolving integration ID: {}", integrationId);
                
                // Get integration configuration from database with sensitive data
                IntegrationDto integrationDto = integrationService.getIntegrationById(integrationId, userId, true);
                
                // Log detailed config parameters for debugging
                logger.info("🔧 [IntegrationResolutionHelper] Integration '{}' (type: {}) has {} config parameters:", 
                    integrationId, integrationDto.getType(), 
                    integrationDto.getConfigParams() != null ? integrationDto.getConfigParams().size() : 0);
                
                if (integrationDto.getConfigParams() != null) {
                    for (var param : integrationDto.getConfigParams()) {
                        logger.info("  📋 Parameter: {}={}", param.getParamKey(), 
                            param.isSensitive() ? "[SENSITIVE]" : param.getParamValue());
                    }
                }
                
                // Convert to JSONObject format expected by ServerManagedIntegrationsModule
                JSONObject integrationConfig = IntegrationConfigMapper.mapIntegrationConfig(integrationDto, configurationLoader);
                
                // Log the final mapped configuration
                logger.info("🎯 [IntegrationResolutionHelper] Final mapped config for integration '{}' (type: {}): {}", 
                    integrationId, integrationDto.getType(), 
                    integrationConfig.keySet().stream()
                        .map(key -> key + "=" + (key.toLowerCase().contains("key") || key.toLowerCase().contains("token") || key.toLowerCase().contains("password") ? "[SENSITIVE]" : integrationConfig.get(key)))
                        .toList());
                
                // Use integration type as key for ServerManagedIntegrationsModule compatibility
                resolved.put(integrationDto.getType(), integrationConfig);
                
                logger.info("✅ [IntegrationResolutionHelper] Successfully resolved integration ID '{}' as type '{}' with {} config parameters", 
                    integrationId, integrationDto.getType(), integrationConfig.length());
                
                // Record usage
                integrationService.recordIntegrationUsage(integrationId);
                
            } catch (Exception e) {
                logger.error("❌ [IntegrationResolutionHelper] Failed to resolve integration ID '{}': {}", integrationId, e.getMessage(), e);
                // Continue with other integrations even if one fails
            }
        }
        
        return resolved;
    }

    /**
     * Resolves a single integration ID to configuration object.
     * 
     * @param integrationId The integration ID to resolve
     * @param userId The user ID for access control
     * @return JSONObject containing resolved integration configuration
     * @throws RuntimeException if integration resolution fails
     */
    public JSONObject resolveSingleIntegrationId(String integrationId, String userId) {
        JSONObject resolved = resolveIntegrationIds(List.of(integrationId), userId);
        if (resolved.length() == 0) {
            throw new RuntimeException("Failed to resolve integration ID: " + integrationId);
        }
        return resolved;
    }

    /**
     * Finds the user's first AI integration for automatic selection when no specific integration is provided.
     * 
     * @param userId The user ID to find AI integrations for
     * @return IntegrationDto of the first AI integration, or null if none found
     */
    public IntegrationDto findUserFirstAIIntegration(String userId) {
        try {
            logger.info("🔍 [IntegrationResolutionHelper] Finding first AI integration for user: {}", userId);
            
            // Define AI integration types
            Set<String> aiIntegrationTypes = Set.of("gemini", "dial", "openai", "claude", "gpt", "ollama");
            
            // Get all user integrations
            List<IntegrationDto> userIntegrations = integrationService.getIntegrationsForUser(userId);
            
            logger.info("📋 [IntegrationResolutionHelper] User has {} total integrations", userIntegrations.size());
            
            // Filter for AI integrations and get the first one (oldest created)
            IntegrationDto firstAI = userIntegrations.stream()
                .filter(integration -> aiIntegrationTypes.contains(integration.getType().toLowerCase()))
                .filter(IntegrationDto::isEnabled) // Only enabled integrations
                .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt())) // Sort by creation date (oldest first)
                .findFirst()
                .orElse(null);
            
            if (firstAI != null) {
                logger.info("✅ [IntegrationResolutionHelper] Found first AI integration: {} (type: {}, created: {})", 
                    firstAI.getId(), firstAI.getType(), firstAI.getCreatedAt());
                return firstAI;
            } else {
                logger.warn("⚠️ [IntegrationResolutionHelper] No AI integrations found for user: {}", userId);
                return null;
            }
            
        } catch (Exception e) {
            logger.error("❌ [IntegrationResolutionHelper] Failed to find AI integration for user {}: {}", userId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Resolves the user's first AI integration to configuration object for automatic AI selection.
     * 
     * @param userId The user ID for access control
     * @return JSONObject containing resolved integration configuration
     * @throws RuntimeException if no AI integrations are found
     */
    public JSONObject resolveUserFirstAIIntegration(String userId) {
        IntegrationDto firstAI = findUserFirstAIIntegration(userId);
        if (firstAI == null) {
            throw new RuntimeException("No AI integrations found for user. Please create an AI integration (Gemini, Claude, OpenAI, etc.) first.");
        }
        
        logger.info("🎯 [IntegrationResolutionHelper] Using first AI integration {} for automatic selection", firstAI.getId());
        return resolveSingleIntegrationId(firstAI.getId(), userId);
    }
}
