package com.github.istin.dmtools.auth.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing an integration with external systems.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "integrations")
public class Integration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    @Column(nullable = false)
    private String type;
    
    @Column(nullable = false)
    private boolean enabled = true;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(nullable = false)
    private long usageCount = 0;
    
    @Column(nullable = true)
    private LocalDateTime lastUsedAt;
    
    @OneToMany(mappedBy = "integration", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<IntegrationConfig> configParams = new HashSet<>();
    
    @OneToMany(mappedBy = "integration", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<IntegrationWorkspace> workspaces = new HashSet<>();
    
    @OneToMany(mappedBy = "integration", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<IntegrationUser> users = new HashSet<>();
    
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
     * Increment the usage count and update the last used timestamp
     */
    public void recordUsage() {
        this.usageCount++;
        this.lastUsedAt = LocalDateTime.now();
    }
} 