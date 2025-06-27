package com.github.istin.dmtools.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Size;
import java.util.HashMap;
import java.util.Map;

/**
 * DTO for updating an existing integration.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateIntegrationRequest {
    
    @Size(min = 1, max = 100, message = "Integration name must be between 1 and 100 characters")
    private String name;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    private Boolean enabled;
    
    /**
     * Configuration parameters to update.
     * Key is the parameter name, value is the parameter value.
     * If a parameter is not included, it will not be updated.
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