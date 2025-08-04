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
        
        // Set User entity fields (now eagerly loaded via JOIN FETCH)
        if (jobConfig.getCreatedBy() != null) {
            dto.setCreatedById(jobConfig.getCreatedBy().getId());
            dto.setCreatedByName(jobConfig.getCreatedBy().getName());
            dto.setCreatedByEmail(jobConfig.getCreatedBy().getEmail());
        }
        
        dto.setEnabled(jobConfig.isEnabled());
        dto.setExecutionCount(jobConfig.getExecutionCount());
        dto.setCreatedAt(jobConfig.getCreatedAt());
        dto.setUpdatedAt(jobConfig.getUpdatedAt());
        dto.setLastExecutedAt(jobConfig.getLastExecutedAt());
        
        // Parse JSON strings to JsonNode objects
        // Handle both proper JSON strings and corrupted OID references from LOB migration
        try {
            ObjectMapper mapper = getObjectMapper();
            
            // Handle jobParameters
            dto.setJobParameters(parseJsonField(jobConfig.getJobParameters(), mapper));
            
            // Handle integrationMappings
            dto.setIntegrationMappings(parseJsonField(jobConfig.getIntegrationMappings(), mapper));
            
        } catch (Exception e) {
            // If JSON parsing fails, create empty objects instead of null
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
    
    /**
     * Parse JSON field handling both proper JSON strings and corrupted OID references.
     * This method fixes the issue where LOB to TEXT migration caused OID references
     * instead of actual JSON content.
     * 
     * @param fieldValue The field value from database (could be JSON string or OID reference)
     * @param mapper ObjectMapper instance
     * @return JsonNode object
     */
    private static JsonNode parseJsonField(String fieldValue, ObjectMapper mapper) {
        if (fieldValue == null || fieldValue.trim().isEmpty()) {
            return mapper.createObjectNode();
        }
        
        String trimmedValue = fieldValue.trim();
        
        // Check if the value is a numeric OID reference (corrupted data from LOB migration)
        if (trimmedValue.matches("^[0-9]+$")) {
            // This is a corrupted OID reference, return empty object
            return mapper.createObjectNode();
        }
        
        try {
            // Try to parse as JSON
            return mapper.readTree(trimmedValue);
        } catch (Exception e) {
            // If parsing fails, return empty object
            return mapper.createObjectNode();
        }
    }
} 