package com.github.istin.dmtools.server.repository;

import com.github.istin.dmtools.auth.model.User;
import com.github.istin.dmtools.server.model.JobConfiguration;
import com.github.istin.dmtools.server.model.WebhookKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WebhookKeyRepository extends JpaRepository<WebhookKey, String> {
    
    /**
     * Find webhook key by hash for authentication.
     */
    Optional<WebhookKey> findByKeyHashAndEnabledTrue(String keyHash);
    
    /**
     * Find webhook keys by job configuration.
     */
    List<WebhookKey> findByJobConfiguration(JobConfiguration jobConfiguration);
    
    /**
     * Find webhook keys by job configuration and enabled status.
     */
    List<WebhookKey> findByJobConfigurationAndEnabledTrue(JobConfiguration jobConfiguration);
    
    /**
     * Find webhook key by ID and job configuration (for access control).
     */
    Optional<WebhookKey> findByIdAndJobConfiguration(String id, JobConfiguration jobConfiguration);
    
    /**
     * Find webhook keys by creator.
     */
    List<WebhookKey> findByCreatedBy(User user);
    
    /**
     * Find webhook key by hash and job configuration for validation.
     */
    @Query("SELECT wk FROM WebhookKey wk WHERE wk.keyHash = :keyHash AND wk.jobConfiguration.id = :jobConfigId AND wk.enabled = true")
    Optional<WebhookKey> findByKeyHashAndJobConfigurationId(@Param("keyHash") String keyHash, @Param("jobConfigId") String jobConfigId);
    
    /**
     * Find webhook keys by job configuration ID and creator for access control.
     */
    @Query("SELECT wk FROM WebhookKey wk WHERE wk.jobConfiguration.id = :jobConfigId AND wk.createdBy = :user")
    List<WebhookKey> findByJobConfigurationIdAndCreatedBy(@Param("jobConfigId") String jobConfigId, @Param("user") User user);
    
    /**
     * Count webhook keys by job configuration.
     */
    long countByJobConfiguration(JobConfiguration jobConfiguration);
    
    /**
     * Count enabled webhook keys by job configuration.
     */
    long countByJobConfigurationAndEnabledTrue(JobConfiguration jobConfiguration);
    
    /**
     * Check if a webhook key exists for a specific job configuration and name.
     */
    boolean existsByJobConfigurationAndNameIgnoreCase(JobConfiguration jobConfiguration, String name);
}