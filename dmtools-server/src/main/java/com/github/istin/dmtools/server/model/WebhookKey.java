package com.github.istin.dmtools.server.model;

import com.github.istin.dmtools.auth.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing an API key for webhook authentication.
 * API keys are scoped to specific job configurations for enhanced security.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "webhook_keys", indexes = {
    @Index(name = "idx_webhook_key_hash", columnList = "key_hash"),
    @Index(name = "idx_webhook_key_job_config", columnList = "job_configuration_id"),
    @Index(name = "idx_webhook_key_created_by", columnList = "created_by")
})
public class WebhookKey {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_configuration_id", nullable = false)
    private JobConfiguration jobConfiguration;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
    
    @Column(name = "key_hash", nullable = false, unique = true, length = 64)
    private String keyHash; // SHA-256 hash of the actual API key
    
    @Column(name = "key_prefix", nullable = false, length = 10)
    private String keyPrefix; // First few characters for identification (e.g., "wk_123...")
    
    @Column(nullable = false)
    private boolean enabled = true;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
    
    @Column(name = "usage_count", nullable = false)
    private long usageCount = 0;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    /**
     * Record usage of this webhook key.
     * Updates usage count and last used timestamp.
     */
    public void recordUsage() {
        this.usageCount++;
        this.lastUsedAt = LocalDateTime.now();
    }
    
    /**
     * Check if the key is active (enabled).
     */
    public boolean isActive() {
        return enabled;
    }
    
    /**
     * Disable the webhook key.
     */
    public void disable() {
        this.enabled = false;
    }
}