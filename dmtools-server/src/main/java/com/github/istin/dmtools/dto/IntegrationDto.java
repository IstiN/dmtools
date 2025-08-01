package com.github.istin.dmtools.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.istin.dmtools.auth.model.Integration;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DTO for Integration entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IntegrationDto {
    
    private String id;
    private String name;
    private String description;
    private String type;
    private boolean enabled;
    private String createdById;
    private String createdByName;
    private String createdByEmail;
    private long usageCount;
    
    /**
     * Categories this integration belongs to (e.g., TrackerClient, AI, Documentation).
     * Populated from integration type configuration.
     */
    private List<String> categories = new ArrayList<>();
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastUsedAt;
    
    private Set<IntegrationConfigDto> configParams = new HashSet<>();
    private Set<WorkspaceDto> workspaces = new HashSet<>();
    private Set<IntegrationUserDto> users = new HashSet<>();
    
    /**
     * Converts an Integration entity to DTO, excluding sensitive configuration values.
     * 
     * @param integration The integration entity
     * @return The integration DTO
     */
    public static IntegrationDto fromEntity(Integration integration) {
        IntegrationDto dto = new IntegrationDto();
        dto.setId(integration.getId());
        dto.setName(integration.getName());
        dto.setDescription(integration.getDescription());
        dto.setType(integration.getType());
        dto.setEnabled(integration.isEnabled());
        dto.setCreatedById(integration.getCreatedBy().getId());
        dto.setCreatedByName(integration.getCreatedBy().getName());
        dto.setCreatedByEmail(integration.getCreatedBy().getEmail());
        dto.setUsageCount(integration.getUsageCount());
        dto.setCreatedAt(integration.getCreatedAt());
        dto.setUpdatedAt(integration.getUpdatedAt());
        dto.setLastUsedAt(integration.getLastUsedAt());
        
        // Convert config params, excluding sensitive values
        dto.setConfigParams(integration.getConfigParams().stream()
                .map(config -> {
                    IntegrationConfigDto configDto = new IntegrationConfigDto();
                    configDto.setId(config.getId());
                    configDto.setParamKey(config.getParamKey());
                    // Only include the value if it's not sensitive
                    if (!config.isSensitive()) {
                        configDto.setParamValue(config.getParamValue());
                    }
                    configDto.setSensitive(config.isSensitive());
                    return configDto;
                })
                .collect(Collectors.toSet()));
        
        return dto;
    }
    
    /**
     * Converts an Integration entity to DTO with categories from integration type configuration.
     * 
     * @param integration The integration entity
     * @param categories The categories for this integration type
     * @return The integration DTO with categories
     */
    public static IntegrationDto fromEntityWithCategories(Integration integration, List<String> categories) {
        IntegrationDto dto = fromEntity(integration);
        dto.setCategories(new ArrayList<>(categories));
        return dto;
    }
    
    /**
     * Converts an Integration entity to DTO, including all configuration values.
     * This should only be used when the user has permission to see sensitive values.
     * 
     * @param integration The integration entity
     * @return The integration DTO with all configuration values
     */
    public static IntegrationDto fromEntityWithSensitiveData(Integration integration) {
        IntegrationDto dto = fromEntity(integration);
        
        // Include all config params with values
        dto.setConfigParams(integration.getConfigParams().stream()
                .map(config -> {
                    IntegrationConfigDto configDto = new IntegrationConfigDto();
                    configDto.setId(config.getId());
                    configDto.setParamKey(config.getParamKey());
                    configDto.setParamValue(config.getParamValue());
                    configDto.setSensitive(config.isSensitive());
                    return configDto;
                })
                .collect(Collectors.toSet()));
        
        return dto;
    }
    
    /**
     * Converts an Integration entity to DTO with categories and sensitive data.
     * 
     * @param integration The integration entity
     * @param categories The categories for this integration type
     * @return The integration DTO with categories and sensitive data
     */
    public static IntegrationDto fromEntityWithSensitiveDataAndCategories(Integration integration, List<String> categories) {
        IntegrationDto dto = fromEntityWithSensitiveData(integration);
        dto.setCategories(new ArrayList<>(categories));
        return dto;
    }
    
    /**
     * Converts an Integration entity to DTO with manually provided config params.
     * This is a workaround for entity relationship issues.
     * 
     * @param integration The integration entity
     * @param configParams The config params loaded manually
     * @return The integration DTO
     */
    public static IntegrationDto fromEntityWithManualConfig(Integration integration, java.util.List<com.github.istin.dmtools.auth.model.IntegrationConfig> configParams) {
        IntegrationDto dto = new IntegrationDto();
        dto.setId(integration.getId());
        dto.setName(integration.getName());
        dto.setDescription(integration.getDescription());
        dto.setType(integration.getType());
        dto.setEnabled(integration.isEnabled());
        dto.setCreatedById(integration.getCreatedBy().getId());
        dto.setCreatedByName(integration.getCreatedBy().getName());
        dto.setCreatedByEmail(integration.getCreatedBy().getEmail());
        dto.setUsageCount(integration.getUsageCount());
        dto.setCreatedAt(integration.getCreatedAt());
        dto.setUpdatedAt(integration.getUpdatedAt());
        dto.setLastUsedAt(integration.getLastUsedAt());
        
        // Convert manually provided config params, excluding sensitive values
        dto.setConfigParams(configParams.stream()
                .map(config -> {
                    IntegrationConfigDto configDto = new IntegrationConfigDto();
                    configDto.setId(config.getId());
                    configDto.setParamKey(config.getParamKey());
                    // Only include the value if it's not sensitive
                    if (!config.isSensitive()) {
                        configDto.setParamValue(config.getParamValue());
                    }
                    // Note: paramValue remains null for sensitive configs, which Jackson converts to undefined in JSON
                    configDto.setSensitive(config.isSensitive());
                    return configDto;
                })
                .collect(Collectors.toSet()));
        
        return dto;
    }
    
    /**
     * Converts an Integration entity to DTO with manually provided config params and categories.
     * 
     * @param integration The integration entity
     * @param configParams The config params loaded manually
     * @param categories The categories for this integration type
     * @return The integration DTO with categories
     */
    public static IntegrationDto fromEntityWithManualConfigAndCategories(Integration integration, java.util.List<com.github.istin.dmtools.auth.model.IntegrationConfig> configParams, List<String> categories) {
        IntegrationDto dto = fromEntityWithManualConfig(integration, configParams);
        dto.setCategories(new ArrayList<>(categories));
        return dto;
    }
} 