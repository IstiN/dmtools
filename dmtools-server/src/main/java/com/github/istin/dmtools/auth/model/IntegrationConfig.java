package com.github.istin.dmtools.auth.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

/**
 * Entity representing a configuration parameter for an integration.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "integration_configs")
public class IntegrationConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "integration_id", nullable = false)
    private Integration integration;
    
    @Column(nullable = false)
    private String paramKey;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String paramValue;
    
    @Column(nullable = false)
    private boolean sensitive = false;
} 