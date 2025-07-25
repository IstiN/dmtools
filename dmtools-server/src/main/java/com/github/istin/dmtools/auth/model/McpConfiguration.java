package com.github.istin.dmtools.auth.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity representing a Model Context Protocol (MCP) configuration.
 * Users can create MCP configurations that bundle their integrations for external tools.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mcp_configurations")
public class McpConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ElementCollection
    @CollectionTable(
        name = "mcp_configuration_integrations",
        joinColumns = @JoinColumn(name = "mcp_configuration_id")
    )
    @Column(name = "integration_id")
    private List<String> integrationIds;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 