package com.github.istin.dmtools.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for job type information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobTypeDto {
    
    /**
     * The job type identifier.
     */
    private String type;
    
    /**
     * Display name for the job type.
     */
    private String displayName;
    
    /**
     * Description of the job type.
     */
    private String description;
    
    /**
     * Icon URL for the job type.
     */
    private String iconUrl;
    
    /**
     * Categories this job belongs to (e.g., AI, Testing, Analysis).
     */
    private List<String> categories = new ArrayList<>();
    
    /**
     * Setup documentation URL for the current locale.
     */
    private String setupDocumentationUrl;
    
    /**
     * Supported execution modes (e.g., STANDALONE, SERVER_MANAGED).
     */
    private List<String> executionModes = new ArrayList<>();
    
    /**
     * Required integrations for this job.
     */
    private List<String> requiredIntegrations = new ArrayList<>();
    
    /**
     * Optional integrations for this job.
     */
    private List<String> optionalIntegrations = new ArrayList<>();
    
    /**
     * List of configuration parameters for this job type.
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
         * Parameter key identifier.
         */
        private String key;
        
        /**
         * Human-readable display name.
         */
        private String displayName;
        
        /**
         * Description of what this parameter does.
         */
        private String description;
        
        /**
         * Instructions for how to configure this parameter.
         */
        private String instructions;
        
        /**
         * Whether this parameter is required.
         */
        private boolean required;
        
        /**
         * Whether this parameter contains sensitive information.
         */
        private boolean sensitive;
        
        /**
         * Input type (text, password, textarea, select, boolean, number, email).
         */
        private String type;
        
        /**
         * Default value for this parameter.
         */
        private String defaultValue;
        
        /**
         * Available options for select type parameters.
         */
        private List<String> options;
        
        /**
         * Example values for this parameter.
         */
        private List<String> examples = new ArrayList<>();
    }
} 