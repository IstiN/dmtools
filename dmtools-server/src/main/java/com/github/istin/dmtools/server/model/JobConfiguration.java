package com.github.istin.dmtools.server.model;

import com.github.istin.dmtools.auth.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing a saved job configuration that users can create and execute.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "job_configurations")
public class JobConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    @Column(nullable = false)
    private String jobType; // "Expert", "TestCasesGenerator", etc.
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
    
    @Column(nullable = false)
    @Lob
    private String jobParameters; // JSON string containing job parameters
    
    @Column(nullable = false)
    @Lob
    private String integrationMappings = "{}"; // JSON string containing integration ID mappings
    
    @Column(nullable = false)
    private boolean enabled = true;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(nullable = true)
    private LocalDateTime lastExecutedAt;
    
    @Column(nullable = false)
    private long executionCount = 0;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Increment the execution count and update the last executed timestamp
     */
    public void recordExecution() {
        this.executionCount++;
        this.lastExecutedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
} 