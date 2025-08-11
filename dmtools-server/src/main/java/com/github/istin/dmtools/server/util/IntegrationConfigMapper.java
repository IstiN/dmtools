package com.github.istin.dmtools.server.util;

import com.github.istin.dmtools.dto.IntegrationDto;
import com.github.istin.dmtools.dto.IntegrationConfigDto;
import com.github.istin.dmtools.auth.service.IntegrationConfigurationLoader;
import com.github.istin.dmtools.auth.model.integration.IntegrationTypeConfig;
import com.github.istin.dmtools.auth.model.integration.ConfigParamConfig;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * Utility class for mapping integration configuration from database format to ServerManagedIntegrationsModule format.
 * This class contains the common logic used by both JobExecutionController and DynamicMCPController.
 */
public class IntegrationConfigMapper {
    
    private static final Logger logger = LoggerFactory.getLogger(IntegrationConfigMapper.class);
    
    /**
     * Maps integration configuration from database format to ServerManagedIntegrationsModule format.
     * This method automatically maps parameters based on the JSON configuration, eliminating 
     * the need for hardcoded mapping logic.
     * 
     * @param integrationDto The integration configuration from database
     * @param configurationLoader The configuration loader to get JSON mapping rules
     * @return JSONObject in the format expected by ServerManagedIntegrationsModule
     */
    public static JSONObject mapIntegrationConfig(IntegrationDto integrationDto, IntegrationConfigurationLoader configurationLoader) {
        JSONObject config = new JSONObject();
        
        // Create a map of config parameters for easy lookup
        Map<String, String> params = new HashMap<>();
        if (integrationDto.getConfigParams() != null) {
            for (IntegrationConfigDto param : integrationDto.getConfigParams()) {
                params.put(param.getParamKey(), param.getParamValue());
            }
        }
        
        logger.info("🔧 Auto-mapping integration config for type '{}' with parameters: {}", 
            integrationDto.getType(), params.keySet());
        
        try {
            // Get the integration type configuration from JSON
            IntegrationTypeConfig typeConfig = configurationLoader.getRawConfiguration(integrationDto.getType());
            
            // Automatically map parameters based on JSON configuration
            mapParametersAutomatically(config, params, typeConfig, integrationDto.getType());
            
        } catch (Exception e) {
            logger.error("Failed to auto-map integration config for type '{}': {}", integrationDto.getType(), e.getMessage());
            // Fallback to direct parameter copying
            for (Map.Entry<String, String> entry : params.entrySet()) {
                config.put(entry.getKey(), entry.getValue());
            }
        }
        
        return config;
    }
    
    /**
     * Simply copies parameters directly from database to output configuration.
     * No mapping or transformation needed - ServerManagedIntegrationsModule now accepts JSON parameter names directly.
     */
    private static void mapParametersAutomatically(JSONObject config, Map<String, String> params, 
                                                   IntegrationTypeConfig typeConfig, String integrationType) {
        
        logger.info("🤖 Direct parameter mapping for integration type: {}", integrationType);
        
        for (ConfigParamConfig paramConfig : typeConfig.getConfigParams()) {
            String paramKey = paramConfig.getKey();
            String paramValue = params.get(paramKey);
            
            if (paramValue != null) {
                // Use parameter name as-is - no mapping needed
                config.put(paramKey, paramValue);
                
                logger.info("  ✅ Mapped '{}': {}", 
                    paramKey, paramConfig.isSensitive() ? "[SENSITIVE]" : paramValue);
            }
        }
    }
    
    /**
     * Checks if an integration is ready for job execution by validating required parameters.
     * This method allows configurations to be saved even with missing required parameters
     * but prevents job execution when required parameters are missing.
     * 
     * @param integrationDto The integration configuration from database
     * @param configurationLoader The configuration loader to get JSON requirements
     * @return List of missing required parameter keys (empty if ready for execution)
     */
    public static List<String> validateIntegrationForExecution(IntegrationDto integrationDto, IntegrationConfigurationLoader configurationLoader) {
        // Create a map of provided parameters
        Map<String, String> providedParams = new HashMap<>();
        if (integrationDto.getConfigParams() != null) {
            for (IntegrationConfigDto param : integrationDto.getConfigParams()) {
                providedParams.put(param.getParamKey(), param.getParamValue());
            }
        }
        
        // Check for missing required parameters
        List<String> missingRequired = configurationLoader.validateRequiredParametersForExecution(
            integrationDto.getType(), providedParams);
        
        if (!missingRequired.isEmpty()) {
            logger.warn("❌ Integration '{}' (type: {}) is missing required parameters: {}. Cannot execute jobs.", 
                integrationDto.getName(), integrationDto.getType(), missingRequired);
        } else {
            logger.info("✅ Integration '{}' (type: {}) has all required parameters for job execution", 
                integrationDto.getName(), integrationDto.getType());
        }
        
        return missingRequired;
    }
} 