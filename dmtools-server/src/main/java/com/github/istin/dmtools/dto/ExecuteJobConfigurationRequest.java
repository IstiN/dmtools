package com.github.istin.dmtools.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for executing a saved job configuration.
 * Supports parameter overrides and integration mapping overrides.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteJobConfigurationRequest {
    
    /**
     * Optional parameter overrides.
     * These will be merged with the saved job parameters.
     * Override values take precedence over saved values.
     */
    private JsonNode parameterOverrides;
    
    /**
     * Optional integration mapping overrides.
     * These will be merged with the saved integration mappings.
     * Override values take precedence over saved values.
     */
    private JsonNode integrationOverrides;
    
    /**
     * Optional flag to specify execution mode.
     * Defaults to HYBRID if not specified.
     */
    private String executionMode;
} 