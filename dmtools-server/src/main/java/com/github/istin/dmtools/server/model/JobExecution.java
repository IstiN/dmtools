package com.github.istin.dmtools.server.model;

import com.github.istin.dmtools.auth.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a job execution instance.
 * Tracks the execution state, timing, and results of individual job runs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "job_executions", indexes = {
    @Index(name = "idx_execution_user_started", columnList = "user_id, started_at"),
    @Index(name = "idx_execution_status", columnList = "status"),
    @Index(name = "idx_execution_job_config", columnList = "job_configuration_id")
})
public class JobExecution {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_configuration_id", nullable = false)
    private JobConfiguration jobConfiguration;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExecutionStatus status;
    
    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "thread_name", length = 100)
    private String threadName;
    
    @Column(name = "execution_parameters", nullable = false, columnDefinition = "TEXT")
    private String executionParameters; // JSON string containing execution parameters
    
    @Column(name = "result_summary", columnDefinition = "TEXT")
    private String resultSummary;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @PrePersist
    protected void onCreate() {
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = ExecutionStatus.PENDING;
        }
    }
    
    /**
     * Calculate the duration of the execution in milliseconds.
     * Returns null if the execution hasn't completed yet.
     */
    public Long getDurationMillis() {
        if (completedAt == null || startedAt == null) {
            return null;
        }
        return java.time.Duration.between(startedAt, completedAt).toMillis();
    }
    
    /**
     * Helper method to mark execution as running.
     */
    public void markAsRunning() {
        this.status = ExecutionStatus.RUNNING;
        this.threadName = Thread.currentThread().getName();
    }
    
    /**
     * Helper method to mark execution as completed successfully.
     */
    public void markAsCompleted(String summary) {
        this.status = ExecutionStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.resultSummary = summary;
    }
    
    /**
     * Helper method to mark execution as failed.
     */
    public void markAsFailed(String error) {
        this.status = ExecutionStatus.FAILED;
        this.completedAt = LocalDateTime.now();
        this.errorMessage = error;
    }
    
    /**
     * Helper method to mark execution as cancelled.
     */
    public void markAsCancelled() {
        this.status = ExecutionStatus.CANCELLED;
        this.completedAt = LocalDateTime.now();
    }
    
    /**
     * Check if the execution is currently active (not in a final state).
     */
    public boolean isActive() {
        return status == ExecutionStatus.PENDING || status == ExecutionStatus.RUNNING;
    }
    
    /**
     * Check if the execution has completed (successfully or with failure).
     */
    public boolean isCompleted() {
        return status == ExecutionStatus.COMPLETED || 
               status == ExecutionStatus.FAILED || 
               status == ExecutionStatus.CANCELLED;
    }

    @OneToMany(mappedBy = "execution", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<JobExecutionLog> logs = new HashSet<>();
} 