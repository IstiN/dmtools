package com.github.istin.dmtools.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for creating a new job configuration.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateJobConfigurationRequest {
    
    @NotBlank(message = "Job configuration name is required")
    private String name;
    
    private String description;
    
    @NotBlank(message = "Job type is required")
    private String jobType;
    
    @NotNull(message = "Job parameters are required")
    private JsonNode jobParameters;
    
    @NotNull(message = "Integration mappings are required")
    private JsonNode integrationMappings;
    
    private Boolean enabled = true;
} 