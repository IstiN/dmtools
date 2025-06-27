package com.github.istin.dmtools.auth.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a relationship between an integration and a workspace.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "integration_workspaces", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"integration_id", "workspace_id"}))
public class IntegrationWorkspace {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "integration_id", nullable = false)
    private Integration integration;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;
    
    @Column(nullable = false)
    private LocalDateTime addedAt;
    
    @PrePersist
    protected void onCreate() {
        addedAt = LocalDateTime.now();
    }
} 