package com.github.istin.dmtools.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
        
        // Handle User entity safely to avoid LazyInitializationException
        try {
            if (jobConfig.getCreatedBy() != null) {
                dto.setCreatedById(jobConfig.getCreatedBy().getId());
                dto.setCreatedByName(jobConfig.getCreatedBy().getName());
                dto.setCreatedByEmail(jobConfig.getCreatedBy().getEmail());
            }
        } catch (Exception e) {
            System.err.println("ERROR accessing User entity: " + e.getMessage());
            // Set defaults if User entity cannot be accessed
            dto.setCreatedById("unknown");
            dto.setCreatedByName("Unknown User");
            dto.setCreatedByEmail("unknown@example.com");
        }
        
        dto.setEnabled(jobConfig.isEnabled());
        dto.setExecutionCount(jobConfig.getExecutionCount());
        dto.setCreatedAt(jobConfig.getCreatedAt());
        dto.setUpdatedAt(jobConfig.getUpdatedAt());
        dto.setLastExecutedAt(jobConfig.getLastExecutedAt());
        
        // Parse JSON strings to JsonNode objects
        try {
            // Use static ObjectMapper instance to avoid Spring context issues
            ObjectMapper mapper = getObjectMapper();
            
            // Debug logging
            System.err.println("DEBUG: Processing job config ID: " + jobConfig.getId());
            System.err.println("DEBUG: jobParameters raw: " + jobConfig.getJobParameters());
            System.err.println("DEBUG: integrationMappings raw: " + jobConfig.getIntegrationMappings());
            
            // Handle jobParameters
            if (jobConfig.getJobParameters() != null && !jobConfig.getJobParameters().trim().isEmpty()) {
                dto.setJobParameters(mapper.readTree(jobConfig.getJobParameters()));
            } else {
                dto.setJobParameters(mapper.createObjectNode());
            }
            
            // Handle integrationMappings
            if (jobConfig.getIntegrationMappings() != null && !jobConfig.getIntegrationMappings().trim().isEmpty()) {
                dto.setIntegrationMappings(mapper.readTree(jobConfig.getIntegrationMappings()));
            } else {
                dto.setIntegrationMappings(mapper.createObjectNode());
            }
        } catch (Exception e) {
            // If JSON parsing fails, create empty objects instead of null
            // Log the error for debugging
            System.err.println("ERROR in JobConfigurationDto.fromEntity: " + e.getMessage());
            e.printStackTrace();
            ObjectMapper mapper = getObjectMapper();
            dto.setJobParameters(mapper.createObjectNode());
            dto.setIntegrationMappings(mapper.createObjectNode());
        }
        
        return dto;
    }
    
    /**
     * Get a static ObjectMapper instance to avoid Spring context issues
     */
    private static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER_INSTANCE;
    }
    
    private static final ObjectMapper OBJECT_MAPPER_INSTANCE = new com.fasterxml.jackson.databind.ObjectMapper();
} 