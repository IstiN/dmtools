package com.github.istin.dmtools.auth.repository;

import com.github.istin.dmtools.auth.model.Integration;
import com.github.istin.dmtools.auth.model.IntegrationConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IntegrationConfigRepository extends JpaRepository<IntegrationConfig, String> {
    
    /**
     * Find all configuration parameters for an integration
     */
    List<IntegrationConfig> findByIntegration(Integration integration);
    
    /**
     * Find a specific configuration parameter by key for an integration
     */
    Optional<IntegrationConfig> findByIntegrationAndParamKey(Integration integration, String paramKey);
    
    /**
     * Delete all configuration parameters for an integration
     */
    void deleteByIntegration(Integration integration);
} 