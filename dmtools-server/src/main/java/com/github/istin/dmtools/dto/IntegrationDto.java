package com.github.istin.dmtools.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.istin.dmtools.auth.model.Integration;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
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
} 