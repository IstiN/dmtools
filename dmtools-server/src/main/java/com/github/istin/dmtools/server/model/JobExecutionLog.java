package com.github.istin.dmtools.server.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing individual log entries generated during job execution.
 * Stores log messages with levels, timestamps, and optional context information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "job_execution_logs", indexes = {
    @Index(name = "idx_log_execution_timestamp", columnList = "execution_id, timestamp"),
    @Index(name = "idx_log_level_timestamp", columnList = "level, timestamp"),
    @Index(name = "idx_log_execution_level", columnList = "execution_id, level")
})
public class JobExecutionLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "execution_id", nullable = false)
    private JobExecution execution;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private LogLevel level;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;
    
    @Column(columnDefinition = "TEXT")
    private String context; // JSON string containing additional context information
    
    @Column(name = "thread_name", length = 100)
    private String threadName;
    
    @Column(name = "component", length = 200)
    private String component; // Name of the component/class that generated the log
    
    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        if (threadName == null) {
            threadName = Thread.currentThread().getName();
        }
    }
    
    /**
     * Convenience constructor for creating log entries with minimal information.
     */
    public JobExecutionLog(JobExecution execution, LogLevel level, String message) {
        this.execution = execution;
        this.level = level;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.threadName = Thread.currentThread().getName();
    }
    
    /**
     * Convenience constructor for creating log entries with component information.
     */
    public JobExecutionLog(JobExecution execution, LogLevel level, String message, String component) {
        this(execution, level, message);
        this.component = component;
    }
    
    /**
     * Convenience constructor for creating log entries with context.
     */
    public JobExecutionLog(JobExecution execution, LogLevel level, String message, String component, String context) {
        this(execution, level, message, component);
        this.context = context;
    }
    
    /**
     * Check if this log entry represents an error.
     */
    public boolean isError() {
        return level == LogLevel.ERROR;
    }
    
    /**
     * Check if this log entry represents a warning.
     */
    public boolean isWarning() {
        return level == LogLevel.WARN;
    }
    
    /**
     * Get a formatted log line string representation.
     */
    public String toFormattedString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(timestamp).append("] ");
        sb.append("[").append(level).append("] ");
        if (component != null) {
            sb.append("[").append(component).append("] ");
        }
        sb.append(message);
        return sb.toString();
    }
} 