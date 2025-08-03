package com.github.istin.dmtools.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for webhook job execution.
 * Supports simplified webhook-specific parameter format.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebhookExecuteRequest {
    
    /**
     * Job-specific parameters to override defaults.
     * This follows the webhook-friendly format specified in the design.
     */
    private JsonNode jobParameters;
    
    /**
     * Integration ID mappings to override defaults.
     * Maps integration names to specific integration IDs.
     */
    private JsonNode integrationMappings;
    
    /**
     * Convert webhook request to ExecuteJobConfigurationRequest format.
     * 
     * @return ExecuteJobConfigurationRequest with mapped parameters
     */
    public ExecuteJobConfigurationRequest toExecuteJobConfigurationRequest() {
        ExecuteJobConfigurationRequest request = new ExecuteJobConfigurationRequest();
        request.setParameterOverrides(this.jobParameters);
        request.setIntegrationOverrides(this.integrationMappings);
        request.setExecutionMode("SERVER_MANAGED"); // Always SERVER_MANAGED for webhook requests
        return request;
    }
}