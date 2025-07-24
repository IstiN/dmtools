package com.github.istin.dmtools.dto;

import com.github.istin.dmtools.server.model.ExecutionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for job execution status queries.
 * Contains detailed information about job execution progress.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobExecutionStatusResponse {
    
    /**
     * Unique execution ID
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
     * Job configuration name (if applicable)
     */
    private String jobConfigurationName;
    
    /**
     * Execution start time
     */
    private LocalDateTime startedAt;
    
    /**
     * Execution completion time
     */
    private LocalDateTime completedAt;
    
    /**
     * Thread name executing the job
     */
    private String threadName;
    
    /**
     * Duration in milliseconds (if completed)
     */
    private Long durationMillis;
    
    /**
     * Execution result summary (if completed successfully)
     */
    private String resultSummary;
    
    /**
     * Error message (if failed)
     */
    private String errorMessage;
    
    /**
     * Execution parameters used for the job
     */
    private String executionParameters;
    
    /**
     * Helper method to check if execution is still active
     */
    public boolean isActive() {
        return status == ExecutionStatus.PENDING || status == ExecutionStatus.RUNNING;
    }
    
    /**
     * Helper method to check if execution is completed
     */
    public boolean isCompleted() {
        return status == ExecutionStatus.COMPLETED || 
               status == ExecutionStatus.FAILED || 
               status == ExecutionStatus.CANCELLED;
    }
} 