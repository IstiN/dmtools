package com.github.istin.dmtools.auth.repository;

import com.github.istin.dmtools.auth.model.Integration;
import com.github.istin.dmtools.auth.model.IntegrationWorkspace;
import com.github.istin.dmtools.auth.model.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IntegrationWorkspaceRepository extends JpaRepository<IntegrationWorkspace, String> {
    
    /**
     * Find all workspace associations for an integration
     */
    List<IntegrationWorkspace> findByIntegration(Integration integration);
    
    /**
     * Find all integration associations for a workspace
     */
    List<IntegrationWorkspace> findByWorkspace(Workspace workspace);
    
    /**
     * Find a specific integration-workspace association
     */
    Optional<IntegrationWorkspace> findByIntegrationAndWorkspace(Integration integration, Workspace workspace);
    
    /**
     * Delete a specific integration-workspace association
     */
    void deleteByIntegrationAndWorkspace(Integration integration, Workspace workspace);
    
    /**
     * Delete all workspace associations for an integration
     */
    void deleteByIntegration(Integration integration);
} 