package com.github.istin.dmtools.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.istin.dmtools.auth.model.integration.IntegrationTypeConfig;
import com.github.istin.dmtools.auth.model.integration.ConfigParamConfig;
import com.github.istin.dmtools.dto.IntegrationTypeDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for loading integration type configurations from JSON files.
 */
@Service
public class IntegrationConfigurationLoader {
    
    private static final Logger logger = LoggerFactory.getLogger(IntegrationConfigurationLoader.class);
    private static final String INTEGRATIONS_LOCATION = "classpath:integrations/*.json";
    
    private final Map<String, IntegrationTypeConfig> configurations = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired
    private IntegrationLocalizationService localizationService;
    
    @PostConstruct
    public void loadConfigurations() {
        try {
            loadConfigurationFiles();
            logger.info("Successfully loaded {} integration type configurations", configurations.size());
        } catch (IOException e) {
            logger.error("Failed to load integration configurations", e);
            throw new IllegalStateException("Failed to load integration configurations", e);
        }
    }
    
    private void loadConfigurationFiles() throws IOException {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources(INTEGRATIONS_LOCATION);
        
        logger.info("Found {} integration configuration files", resources.length);
        
        for (Resource resource : resources) {
            String filename = resource.getFilename();
            if (filename == null) {
                logger.warn("Skipping resource with null filename: {}", resource);
                continue;
            }
            
            String integrationType = filename.substring(0, filename.lastIndexOf('.'));
            
            try (InputStream inputStream = resource.getInputStream()) {
                IntegrationTypeConfig config = objectMapper.readValue(inputStream, IntegrationTypeConfig.class);
                
                // Validate configuration
                validateConfiguration(config, filename);
                
                configurations.put(integrationType, config);
                logger.debug("Loaded configuration for integration type: {}", integrationType);
            } catch (Exception e) {
                logger.error("Failed to load configuration from file: {}", filename, e);
                throw new IOException("Failed to load configuration from file: " + filename, e);
            }
        }
    }
    
    private void validateConfiguration(IntegrationTypeConfig config, String filename) {
        if (config.getType() == null || config.getType().trim().isEmpty()) {
            throw new IllegalArgumentException("Integration type cannot be null or empty in file: " + filename);
        }
        
        // Allow display name to be null if displayNameKey is provided
        if (config.getDisplayName() == null && config.getDisplayNameKey() == null) {
            throw new IllegalArgumentException("Display name or displayNameKey must be provided in file: " + filename);
        }
        
        if (config.getConfigParams() == null) {
            config.setConfigParams(new ArrayList<>());
        }
        
        if (config.getCategories() == null) {
            config.setCategories(new ArrayList<>());
        }
        
        // Validate config parameters
        for (ConfigParamConfig param : config.getConfigParams()) {
            if (param.getKey() == null || param.getKey().trim().isEmpty()) {
                throw new IllegalArgumentException("Config parameter key cannot be null or empty in file: " + filename);
            }
            
            // Allow display name to be null if displayNameKey is provided
            if (param.getDisplayName() == null && param.getDisplayNameKey() == null) {
                throw new IllegalArgumentException("Config parameter display name or displayNameKey must be provided in file: " + filename);
            }
        }
    }
    
    /**
     * Get all available integration types.
     *
     * @return List of integration type DTOs
     */
    public List<IntegrationTypeDto> getAllIntegrationTypes() {
        return getAllIntegrationTypes("en");
    }
    
    /**
     * Get all available integration types with localization.
     *
     * @param locale The locale for localization
     * @return List of integration type DTOs
     */
    public List<IntegrationTypeDto> getAllIntegrationTypes(String locale) {
        return configurations.values().stream()
                .filter(config -> !config.isHidden())
                .map(config -> convertToDto(config, locale))
                .collect(Collectors.toList());
    }
    
    /**
     * Get integrations by category.
     *
     * @param category The category to filter by
     * @return List of integration type DTOs
     */
    public List<IntegrationTypeDto> getIntegrationsByCategory(String category) {
        return getIntegrationsByCategory(category, "en");
    }
    
    /**
     * Get integrations by category with localization.
     *
     * @param category The category to filter by
     * @param locale The locale for localization
     * @return List of integration type DTOs
     */
    public List<IntegrationTypeDto> getIntegrationsByCategory(String category, String locale) {
        return configurations.values().stream()
                .filter(config -> !config.isHidden())
                .filter(config -> config.getCategories().contains(category))
                .map(config -> convertToDto(config, locale))
                .collect(Collectors.toList());
    }
    
    /**
     * Get a specific integration type by its type identifier.
     *
     * @param type The integration type identifier
     * @return The integration type DTO
     * @throws IllegalArgumentException if the integration type is not found
     */
    public IntegrationTypeDto getIntegrationType(String type) {
        return getIntegrationType(type, "en");
    }
    
    /**
     * Get a specific integration type by its type identifier with localization.
     *
     * @param type The integration type identifier
     * @param locale The locale for localization
     * @return The integration type DTO
     * @throws IllegalArgumentException if the integration type is not found
     */
    public IntegrationTypeDto getIntegrationType(String type, String locale) {
        IntegrationTypeConfig config = configurations.get(type);
        if (config == null) {
            throw new IllegalArgumentException("Integration type not found: " + type);
        }
        return convertToDto(config, locale);
    }
    
    /**
     * Check if an integration type exists.
     *
     * @param type The integration type identifier
     * @return true if the integration type exists, false otherwise
     */
    public boolean hasIntegrationType(String type) {
        return configurations.containsKey(type);
    }
    
    /**
     * Get the raw configuration for an integration type.
     *
     * @param type The integration type identifier
     * @return The raw integration type configuration
     * @throws IllegalArgumentException if the integration type is not found
     */
    public IntegrationTypeConfig getRawConfiguration(String type) {
        IntegrationTypeConfig config = configurations.get(type);
        if (config == null) {
            throw new IllegalArgumentException("Integration type not found: " + type);
        }
        return config;
    }
    
    /**
     * Validate that provided parameter keys are allowed for the given integration type.
     *
     * @param integrationType The integration type identifier
     * @param parameterKeys The set of parameter keys to validate
     * @throws IllegalArgumentException if any parameter key is not allowed
     */
    public void validateParameterKeys(String integrationType, Set<String> parameterKeys) {
        IntegrationTypeConfig config = getRawConfiguration(integrationType);
        
        // Get allowed parameter keys from JSON configuration
        Set<String> allowedKeys = config.getConfigParams().stream()
                .map(ConfigParamConfig::getKey)
                .collect(Collectors.toSet());
        
        // Find invalid keys
        Set<String> invalidKeys = parameterKeys.stream()
                .filter(key -> !allowedKeys.contains(key))
                .collect(Collectors.toSet());
        
        if (!invalidKeys.isEmpty()) {
            throw new IllegalArgumentException(
                String.format("Invalid parameter keys for integration type '%s': %s. Allowed keys: %s", 
                    integrationType, invalidKeys, allowedKeys)
            );
        }
        
        logger.debug("Parameter validation passed for integration type '{}' with keys: {}", 
            integrationType, parameterKeys);
    }
    
    /**
     * Get all available categories.
     *
     * @return Set of all categories
     */
    public Set<String> getAllCategories() {
        return configurations.values().stream()
                .flatMap(config -> config.getCategories().stream())
                .collect(Collectors.toSet());
    }
    
    private IntegrationTypeDto convertToDto(IntegrationTypeConfig config) {
        return convertToDto(config, "en");
    }
    
    private IntegrationTypeDto convertToDto(IntegrationTypeConfig config, String locale) {
        IntegrationTypeDto dto = new IntegrationTypeDto();
        dto.setType(config.getType());
        
        // Use localized display name if key is provided, otherwise use direct value
        if (config.getDisplayNameKey() != null) {
            dto.setDisplayName(localizationService.getMessage(config.getDisplayNameKey(), locale));
        } else {
            dto.setDisplayName(config.getDisplayName());
        }
        
        // Use localized description if key is provided, otherwise use direct value
        if (config.getDescriptionKey() != null) {
            dto.setDescription(localizationService.getMessage(config.getDescriptionKey(), locale));
        } else {
            dto.setDescription(config.getDescription());
        }
        
        dto.setIconUrl(config.getIconUrl());
        dto.setCategories(new ArrayList<>(config.getCategories()));
        
        // Add setup documentation links
        if (config.getSetupDocumentation() != null) {
            String setupDoc = config.getSetupDocumentation().get(locale);
            if (setupDoc == null) {
                // Fallback to default locale
                setupDoc = config.getSetupDocumentation().get(localizationService.getDefaultLocale());
            }
            dto.setSetupDocumentationUrl(setupDoc);
        }
        
        // Convert config parameters
        List<IntegrationTypeDto.ConfigParamDefinition> paramDefs = config.getConfigParams().stream()
                .map(paramConfig -> convertConfigParamToDto(paramConfig, locale))
                .collect(Collectors.toList());
        
        dto.setConfigParams(paramDefs);
        
        // Set MCP support from configuration
        dto.setSupportsMcp(config.isSupportsMcp());
        
        return dto;
    }
    
    private IntegrationTypeDto.ConfigParamDefinition convertConfigParamToDto(ConfigParamConfig config, String locale) {
        IntegrationTypeDto.ConfigParamDefinition def = new IntegrationTypeDto.ConfigParamDefinition();
        def.setKey(config.getKey());
        
        // Use localized display name if key is provided, otherwise use direct value
        if (config.getDisplayNameKey() != null) {
            def.setDisplayName(localizationService.getMessage(config.getDisplayNameKey(), locale));
        } else {
            def.setDisplayName(config.getDisplayName());
        }
        
        // Use localized description if key is provided, otherwise use direct value
        if (config.getDescriptionKey() != null) {
            def.setDescription(localizationService.getMessage(config.getDescriptionKey(), locale));
        } else {
            def.setDescription(config.getDescriptionKey());
        }
        
        // Use localized instructions if key is provided, otherwise use direct value
        if (config.getInstructionsKey() != null) {
            def.setInstructions(localizationService.getMessage(config.getInstructionsKey(), locale));
        } else {
            def.setInstructions(config.getInstructionsKey());
        }
        
        def.setRequired(config.isRequired());
        def.setSensitive(config.isSensitive());
        def.setDefaultValue(config.getDefaultValue());
        def.setType(config.getInputType());
        
        // Convert options to simple list format
        if (config.getOptions() != null && !config.getOptions().isEmpty()) {
            // Use new options field if available
            def.setOptions(config.getOptions().stream()
                    .map(optionConfig -> optionConfig.getValue())
                    .collect(Collectors.toList()));
        } else if (config.getValidation() != null && config.getValidation().getAllowedValues() != null) {
            // Fallback to validation allowed values for backward compatibility
            def.setOptions(new ArrayList<>(config.getValidation().getAllowedValues()));
        }
        
        return def;
    }
    
    /**
     * Validates that all required parameters are present for job execution.
     * Unlike validateParameterKeys, this allows saving incomplete configurations
     * but prevents execution when required parameters are missing.
     * 
     * @param integrationType The integration type (e.g., "jira", "confluence")
     * @param providedParameters Map of parameter keys to values
     * @return List of missing required parameter keys (empty if all required params are present)
     */
    public List<String> validateRequiredParametersForExecution(String integrationType, Map<String, String> providedParameters) {
        try {
            IntegrationTypeConfig typeConfig = getRawConfiguration(integrationType);
            
            List<String> missingRequired = new ArrayList<>();
            
            for (ConfigParamConfig paramConfig : typeConfig.getConfigParams()) {
                if (paramConfig.isRequired()) {
                    String paramKey = paramConfig.getKey();
                    String paramValue = providedParameters.get(paramKey);
                    
                    // Consider parameter missing if not provided or empty/blank
                    if (paramValue == null || paramValue.trim().isEmpty()) {
                        missingRequired.add(paramKey);
                    }
                }
            }
            
            if (!missingRequired.isEmpty()) {
                logger.warn("⚠️ Missing required parameters for integration type '{}': {}", 
                    integrationType, missingRequired);
            } else {
                logger.info("✅ All required parameters present for integration type '{}'", integrationType);
            }
            
            return missingRequired;
            
        } catch (IllegalArgumentException e) {
            logger.error("Failed to validate required parameters for integration type '{}': {}", 
                integrationType, e.getMessage());
            // Return empty list to allow execution in case of configuration errors
            return new ArrayList<>();
        }
    }
} 