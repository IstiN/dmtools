package com.github.istin.dmtools.dto;

import com.github.istin.dmtools.server.model.ExecutionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for asynchronous job execution.
 * Returns immediate response with execution ID and status.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobExecutionResponse {
    
    /**
     * Unique execution ID for tracking the job
     */
    private String executionId;
    
    /**
     * Current execution status
     */
    private ExecutionStatus status;
    
    /**
     * Job name being executed
     */
    private String jobName;
    
    /**
     * Job configuration ID (if applicable)
     */
    private String jobConfigurationId;
    
    /**
     * Execution start time
     */
    private LocalDateTime startedAt;
    
    /**
     * Estimated completion time (optional)
     */
    private LocalDateTime estimatedCompletionAt;
    
    /**
     * Message about the execution
     */
    private String message;
    
    /**
     * Create a success response for started execution
     */
    public static JobExecutionResponse started(String executionId, String jobName, String jobConfigurationId) {
        JobExecutionResponse response = new JobExecutionResponse();
        response.setExecutionId(executionId);
        response.setStatus(ExecutionStatus.PENDING);
        response.setJobName(jobName);
        response.setJobConfigurationId(jobConfigurationId);
        response.setStartedAt(LocalDateTime.now());
        response.setMessage("Job execution started successfully");
        return response;
    }
    
    /**
     * Create an error response
     */
    public static JobExecutionResponse error(String message) {
        JobExecutionResponse response = new JobExecutionResponse();
        response.setStatus(ExecutionStatus.FAILED);
        response.setMessage(message);
        return response;
    }
} 