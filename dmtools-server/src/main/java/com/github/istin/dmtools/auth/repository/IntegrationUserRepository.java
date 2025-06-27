package com.github.istin.dmtools.auth.repository;

import com.github.istin.dmtools.auth.model.Integration;
import com.github.istin.dmtools.auth.model.IntegrationUser;
import com.github.istin.dmtools.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IntegrationUserRepository extends JpaRepository<IntegrationUser, String> {
    
    /**
     * Find all user associations for an integration
     */
    List<IntegrationUser> findByIntegration(Integration integration);
    
    /**
     * Find all integration associations for a user
     */
    List<IntegrationUser> findByUser(User user);
    
    /**
     * Find a specific integration-user association
     */
    Optional<IntegrationUser> findByIntegrationAndUser(Integration integration, User user);
    
    /**
     * Delete a specific integration-user association
     */
    void deleteByIntegrationAndUser(Integration integration, User user);
    
    /**
     * Delete all user associations for an integration
     */
    void deleteByIntegration(Integration integration);
} 