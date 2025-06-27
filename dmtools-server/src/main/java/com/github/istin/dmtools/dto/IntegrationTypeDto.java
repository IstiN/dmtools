package com.github.istin.dmtools.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for integration type information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IntegrationTypeDto {
    
    /**
     * The type identifier.
     */
    private String type;
    
    /**
     * Display name for the integration type.
     */
    private String displayName;
    
    /**
     * Description of the integration type.
     */
    private String description;
    
    /**
     * Icon URL for the integration type.
     */
    private String iconUrl;
    
    /**
     * List of configuration parameters for this integration type.
     */
    private List<ConfigParamDefinition> configParams = new ArrayList<>();
    
    /**
     * Inner class representing a configuration parameter definition.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfigParamDefinition {
        /**
         * Parameter key.
         */
        private String key;
        
        /**
         * Display name for the parameter.
         */
        private String displayName;
        
        /**
         * Description of the parameter.
         */
        private String description;
        
        /**
         * Whether the parameter is required.
         */
        private boolean required;
        
        /**
         * Whether the parameter contains sensitive information.
         */
        private boolean sensitive;
        
        /**
         * Default value for the parameter.
         */
        private String defaultValue;
        
        /**
         * Type of the parameter (e.g., "string", "password", "url", "select").
         */
        private String type;
        
        /**
         * Options for select-type parameters.
         */
        private List<String> options;
    }
} 