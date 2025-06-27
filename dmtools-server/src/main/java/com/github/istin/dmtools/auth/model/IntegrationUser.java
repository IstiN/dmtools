package com.github.istin.dmtools.auth.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a relationship between an integration and a user.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "integration_users", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"integration_id", "user_id"}))
public class IntegrationUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "integration_id", nullable = false)
    private Integration integration;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IntegrationPermissionLevel permissionLevel;
    
    @Column(nullable = false)
    private LocalDateTime addedAt;
    
    @PrePersist
    protected void onCreate() {
        addedAt = LocalDateTime.now();
    }
} 