package com.github.istin.dmtools.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an existing job configuration.
 * All fields are optional - only provided fields will be updated.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateJobConfigurationRequest {
    
    private String name;
    private String description;
    private String jobType;
    private JsonNode jobParameters;
    private JsonNode integrationMappings;
    private Boolean enabled;
} 