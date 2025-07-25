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
     * Categories this integration belongs to (e.g., SourceCode, TrackerClient, AI).
     */
    private List<String> categories = new ArrayList<>();
    
    /**
     * Setup documentation URL for the current locale.
     */
    private String setupDocumentationUrl;
    
    /**
     * List of configuration parameters for this integration type.
     */
    private List<ConfigParamDefinition> configParams = new ArrayList<>();
    
    /**
     * Whether this integration type supports MCP protocol.
     */
    private boolean supportsMcp = false;
    
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
         * Instructions for how to obtain or configure this parameter (can contain markdown).
         */
        private String instructions;
        
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
         * Type of the parameter (e.g., "string", "password", "url", "select", "textarea").
         */
        private String type;
        
        /**
         * Options for select-type parameters.
         */
        private List<String> options;
    }
} 