package com.github.istin.dmtools.server.model;

/**
 * Enum representing the different states of a job execution.
 * Used to track the lifecycle of job executions from creation to completion.
 */
public enum ExecutionStatus {
    /**
     * Job execution has been created but not yet started
     */
    PENDING,
    
    /**
     * Job execution is currently running
     */
    RUNNING,
    
    /**
     * Job execution completed successfully
     */
    COMPLETED,
    
    /**
     * Job execution failed with an error
     */
    FAILED,
    
    /**
     * Job execution was cancelled by user or system
     */
    CANCELLED
} 