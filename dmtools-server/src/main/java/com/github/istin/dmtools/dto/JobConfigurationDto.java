package com.github.istin.dmtools.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.istin.dmtools.server.model.JobConfiguration;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for JobConfiguration entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobConfigurationDto {
    
    private String id;
    private String name;
    private String description;
    private String jobType;
    private String createdById;
    private String createdByName;
    private String createdByEmail;
    private JsonNode jobParameters;
    private JsonNode integrationMappings;
    private boolean enabled;
    private long executionCount;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastExecutedAt;
    
    /**
     * Converts a JobConfiguration entity to DTO.
     * 
     * @param jobConfig The job configuration entity
     * @return The job configuration DTO
     */
    public static JobConfigurationDto fromEntity(JobConfiguration jobConfig) {
        JobConfigurationDto dto = new JobConfigurationDto();
        dto.setId(jobConfig.getId());
        dto.setName(jobConfig.getName());
        dto.setDescription(jobConfig.getDescription());
        dto.setJobType(jobConfig.getJobType());
        dto.setCreatedById(jobConfig.getCreatedBy().getId());
        dto.setCreatedByName(jobConfig.getCreatedBy().getName());
        dto.setCreatedByEmail(jobConfig.getCreatedBy().getEmail());
        dto.setEnabled(jobConfig.isEnabled());
        dto.setExecutionCount(jobConfig.getExecutionCount());
        dto.setCreatedAt(jobConfig.getCreatedAt());
        dto.setUpdatedAt(jobConfig.getUpdatedAt());
        dto.setLastExecutedAt(jobConfig.getLastExecutedAt());
        
        // Parse JSON strings to JsonNode objects
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            dto.setJobParameters(mapper.readTree(jobConfig.getJobParameters()));
            dto.setIntegrationMappings(mapper.readTree(jobConfig.getIntegrationMappings()));
        } catch (Exception e) {
            // If JSON parsing fails, set to null - this shouldn't happen in normal operation
            dto.setJobParameters(null);
            dto.setIntegrationMappings(null);
        }
        
        return dto;
    }
} 