package com.github.istin.dmtools.server.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.istin.dmtools.server.model.JobTypeConfig;
import com.github.istin.dmtools.dto.JobTypeDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * Service for loading job type configurations from JSON files.
 */
@Service
public class JobConfigurationLoader {
    
    private static final Logger logger = LoggerFactory.getLogger(JobConfigurationLoader.class);
    private static final String JOBS_LOCATION = "classpath:jobs/*.json";
    
    private final Map<String, JobTypeConfig> configurations = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @PostConstruct
    public void loadConfigurations() {
        try {
            loadConfigurationFiles();
            logger.info("Successfully loaded {} job type configurations", configurations.size());
        } catch (IOException e) {
            logger.error("Failed to load job configurations", e);
            throw new IllegalStateException("Failed to load job configurations", e);
        }
    }
    
    private void loadConfigurationFiles() throws IOException {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources(JOBS_LOCATION);
        
        logger.info("Found {} job configuration files", resources.length);
        
        for (Resource resource : resources) {
            String filename = resource.getFilename();
            if (filename == null) {
                logger.warn("Skipping resource with null filename: {}", resource);
                continue;
            }
            
            String jobType = filename.substring(0, filename.lastIndexOf('.'));
            
            try (InputStream inputStream = resource.getInputStream()) {
                JobTypeConfig config = objectMapper.readValue(inputStream, JobTypeConfig.class);
                
                // Validate configuration
                validateConfiguration(config, filename);
                
                configurations.put(jobType, config);
                logger.debug("Loaded configuration for job type: {}", jobType);
            } catch (Exception e) {
                logger.error("Failed to load configuration from file: {}", filename, e);
                throw new IOException("Failed to load configuration from file: " + filename, e);
            }
        }
    }
    
    private void validateConfiguration(JobTypeConfig config, String filename) {
        if (config.getType() == null || config.getType().trim().isEmpty()) {
            throw new IllegalArgumentException("Job type cannot be null or empty in file: " + filename);
        }
        
        // Allow display name to be null if displayNameKey is provided
        if (config.getDisplayName() == null && config.getDisplayNameKey() == null) {
            // For now, use type as display name fallback
            config.setDisplayName(config.getType());
        }
        
        if (config.getConfigParams() == null) {
            config.setConfigParams(new ArrayList<>());
        }
        
        if (config.getCategories() == null) {
            config.setCategories(new ArrayList<>());
        }
        
        if (config.getExecutionModes() == null) {
            config.setExecutionModes(List.of("STANDALONE"));
        }
        
        if (config.getRequiredIntegrations() == null) {
            config.setRequiredIntegrations(new ArrayList<>());
        }
        
        if (config.getOptionalIntegrations() == null) {
            config.setOptionalIntegrations(new ArrayList<>());
        }
        
        // Validate config parameters
        if (config.getConfigParams() != null) {
            for (JobTypeConfig.ConfigParamConfig param : config.getConfigParams()) {
                if (param.getKey() == null || param.getKey().trim().isEmpty()) {
                    throw new IllegalArgumentException("Config parameter key cannot be null or empty in file: " + filename);
                }
                
                // Allow display name to be null if displayNameKey is provided
                if (param.getDisplayName() == null && param.getDisplayNameKey() == null) {
                    // Use key as display name fallback
                    param.setDisplayName(param.getKey());
                }
            }
        }
    }
    
    /**
     * Get all available job types.
     *
     * @return List of job type DTOs
     */
    public List<JobTypeDto> getAllJobTypes() {
        return getAllJobTypes("en");
    }
    
    /**
     * Get all available job types with localization.
     *
     * @param locale The locale for localization
     * @return List of job type DTOs
     */
    public List<JobTypeDto> getAllJobTypes(String locale) {
        return configurations.values().stream()
                .filter(config -> !config.isHidden())
                .map(config -> convertToDto(config, locale))
                .collect(Collectors.toList());
    }
    
    /**
     * Get jobs by category.
     *
     * @param category The category to filter by
     * @return List of job type DTOs
     */
    public List<JobTypeDto> getJobsByCategory(String category) {
        return getJobsByCategory(category, "en");
    }
    
    /**
     * Get jobs by category with localization.
     *
     * @param category The category to filter by
     * @param locale The locale for localization
     * @return List of job type DTOs
     */
    public List<JobTypeDto> getJobsByCategory(String category, String locale) {
        return configurations.values().stream()
                .filter(config -> !config.isHidden())
                .filter(config -> config.getCategories().contains(category))
                .map(config -> convertToDto(config, locale))
                .collect(Collectors.toList());
    }
    
    /**
     * Get a specific job type by its type identifier.
     *
     * @param type The job type identifier
     * @return The job type DTO
     * @throws IllegalArgumentException if the job type is not found
     */
    public JobTypeDto getJobType(String type) {
        return getJobType(type, "en");
    }
    
    /**
     * Get a specific job type by its type identifier with localization.
     *
     * @param type The job type identifier
     * @param locale The locale for localization
     * @return The job type DTO
     * @throws IllegalArgumentException if the job type is not found
     */
    public JobTypeDto getJobType(String type, String locale) {
        JobTypeConfig config = configurations.get(type);
        if (config == null) {
            throw new IllegalArgumentException("Job type not found: " + type);
        }
        return convertToDto(config, locale);
    }
    
    /**
     * Check if a job type exists.
     *
     * @param type The job type identifier
     * @return true if the job type exists, false otherwise
     */
    public boolean hasJobType(String type) {
        return configurations.containsKey(type);
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
    
    /**
     * Get required integrations for a job type.
     *
     * @param type The job type identifier
     * @return List of required integrations
     */
    public List<String> getRequiredIntegrations(String type) {
        JobTypeConfig config = configurations.get(type);
        if (config == null) {
            throw new IllegalArgumentException("Job type not found: " + type);
        }
        return config.getRequiredIntegrations() != null ? config.getRequiredIntegrations() : new ArrayList<>();
    }
    
    private JobTypeDto convertToDto(JobTypeConfig config, String locale) {
        JobTypeDto dto = new JobTypeDto();
        dto.setType(config.getType());
        
        // Use localized display name if key is provided, otherwise use direct value
        if (config.getDisplayNameKey() != null) {
            // For now, just use the key - later we can add localization service
            dto.setDisplayName(config.getDisplayName() != null ? config.getDisplayName() : config.getType());
        } else {
            dto.setDisplayName(config.getDisplayName() != null ? config.getDisplayName() : config.getType());
        }
        
        // Use localized description if key is provided, otherwise use direct value
        if (config.getDescriptionKey() != null) {
            // For now, just use the description - later we can add localization service
            dto.setDescription(config.getDescription());
        } else {
            dto.setDescription(config.getDescription());
        }
        
        dto.setIconUrl(config.getIconUrl());
        dto.setCategories(new ArrayList<>(config.getCategories()));
        dto.setExecutionModes(new ArrayList<>(config.getExecutionModes()));
        dto.setRequiredIntegrations(new ArrayList<>(config.getRequiredIntegrations()));
        dto.setOptionalIntegrations(new ArrayList<>(config.getOptionalIntegrations()));
        
        // Add setup documentation links
        if (config.getSetupDocumentation() != null) {
            String setupDoc = config.getSetupDocumentation().get(locale);
            if (setupDoc == null) {
                // Fallback to default locale
                setupDoc = config.getSetupDocumentation().get("en");
            }
            dto.setSetupDocumentationUrl(setupDoc);
        }
        
        // Convert config parameters
        List<JobTypeDto.ConfigParamDefinition> paramDefs = config.getConfigParams().stream()
                .map(paramConfig -> convertConfigParamToDto(paramConfig, locale))
                .collect(Collectors.toList());
        
        dto.setConfigParams(paramDefs);
        
        return dto;
    }
    
    private JobTypeDto.ConfigParamDefinition convertConfigParamToDto(JobTypeConfig.ConfigParamConfig config, String locale) {
        JobTypeDto.ConfigParamDefinition def = new JobTypeDto.ConfigParamDefinition();
        def.setKey(config.getKey());
        
        // Use localized display name if key is provided, otherwise use direct value
        if (config.getDisplayNameKey() != null) {
            // For now, just use the display name - later we can add localization service
            def.setDisplayName(config.getDisplayName() != null ? config.getDisplayName() : config.getKey());
        } else {
            def.setDisplayName(config.getDisplayName() != null ? config.getDisplayName() : config.getKey());
        }
        
        // Use localized description if key is provided, otherwise use direct value
        if (config.getDescriptionKey() != null) {
            // For now, just use the description - later we can add localization service
            def.setDescription(config.getDescription());
        } else {
            def.setDescription(config.getDescription());
        }
        
        // Use localized instructions if key is provided, otherwise use direct value
        if (config.getInstructionsKey() != null) {
            // For now, just use the instructions - later we can add localization service
            def.setInstructions(config.getInstructions());
        } else {
            def.setInstructions(config.getInstructions());
        }
        
        def.setRequired(config.isRequired());
        def.setSensitive(config.isSensitive());
        def.setDefaultValue(config.getDefaultValue());
        def.setType(config.getInputType());
        
        // Convert validation allowed values to options
        if (config.getValidation() != null && config.getValidation().getEnumValues() != null) {
            def.setOptions(new ArrayList<>(config.getValidation().getEnumValues()));
        } else if (config.getOptions() != null) {
            def.setOptions(config.getOptions().stream()
                    .map(JobTypeConfig.OptionConfig::getValue)
                    .collect(Collectors.toList()));
        }
        
        // Set examples
        if (config.getExamples() != null) {
            def.setExamples(new ArrayList<>(config.getExamples()));
        }
        
        return def;
    }
} 