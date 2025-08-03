package com.github.istin.dmtools.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for webhook job execution.
 * Provides execution tracking information and status.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebhookExecutionResponse {
    
    private String executionId;
    private String status;
    private String message;
    private String jobConfigurationId;
    
    /**
     * Create a successful webhook execution response.
     * 
     * @param executionId The job execution ID
     * @param jobConfigurationId The job configuration ID
     * @return Success response
     */
    public static WebhookExecutionResponse success(String executionId, String jobConfigurationId) {
        return new WebhookExecutionResponse(
            executionId,
            "PENDING",
            "Job execution started successfully",
            jobConfigurationId
        );
    }
    
    /**
     * Create an error webhook execution response.
     * 
     * @param error The error message
     * @param code The error code
     * @return Error response
     */
    public static WebhookExecutionResponse error(String error, String code) {
        WebhookExecutionResponse response = new WebhookExecutionResponse();
        response.setStatus("ERROR");
        response.setMessage(error);
        response.setJobConfigurationId(null);
        response.setExecutionId(null);
        return response;
    }
}