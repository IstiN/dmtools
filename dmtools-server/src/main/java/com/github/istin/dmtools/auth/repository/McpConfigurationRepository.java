package com.github.istin.dmtools.auth.repository;

import com.github.istin.dmtools.auth.model.McpConfiguration;
import com.github.istin.dmtools.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for MCP Configuration entities.
 * Provides user-scoped access to MCP configurations.
 */
@Repository
public interface McpConfigurationRepository extends JpaRepository<McpConfiguration, String> {
    
    /**
     * Find all MCP configurations for a specific user.
     *
     * @param user The user
     * @return List of MCP configurations owned by the user
     */
    List<McpConfiguration> findByUserOrderByCreatedAtDesc(User user);
    
    /**
     * Find MCP configuration by ID and user (ensures user can only access their own configs).
     *
     * @param id The configuration ID
     * @param user The user
     * @return Optional MCP configuration
     */
    Optional<McpConfiguration> findByIdAndUser(String id, User user);
    
    /**
     * Check if a configuration name already exists for a user.
     *
     * @param name The configuration name
     * @param user The user
     * @return true if name exists for this user
     */
    boolean existsByNameAndUser(String name, User user);
    
    /**
     * Check if a configuration name exists for a user, excluding a specific configuration.
     * Useful for update operations.
     *
     * @param name The configuration name
     * @param user The user
     * @param excludeId The configuration ID to exclude from the check
     * @return true if name exists for this user (excluding the specified config)
     */
    @Query("SELECT COUNT(m) > 0 FROM McpConfiguration m WHERE m.name = :name AND m.user = :user AND m.id != :excludeId")
    boolean existsByNameAndUserAndIdNot(@Param("name") String name, @Param("user") User user, @Param("excludeId") String excludeId);
    
    /**
     * Count total configurations for a user.
     *
     * @param user The user
     * @return Count of configurations
     */
    long countByUser(User user);
} 