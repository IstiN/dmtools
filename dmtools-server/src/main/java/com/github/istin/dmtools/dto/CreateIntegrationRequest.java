package com.github.istin.dmtools.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.HashMap;
import java.util.Map;

/**
 * DTO for creating a new integration.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateIntegrationRequest {
    
    @NotBlank(message = "Integration name is required")
    @Size(min = 1, max = 100, message = "Integration name must be between 1 and 100 characters")
    private String name;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    @NotBlank(message = "Integration type is required")
    private String type;
    
    /**
     * Configuration parameters for the integration.
     * Key is the parameter name, value is the parameter value.
     */
    private Map<String, ConfigParam> configParams = new HashMap<>();
    
    /**
     * Inner class representing a configuration parameter.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfigParam {
        private String value;
        private boolean sensitive;
    }
} 