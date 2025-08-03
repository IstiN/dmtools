package com.github.istin.dmtools.server.repository;

import com.github.istin.dmtools.auth.model.User;
import com.github.istin.dmtools.server.model.JobConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobConfigurationRepository extends JpaRepository<JobConfiguration, String> {
    
    /**
     * Find job configurations by creator
     */
    List<JobConfiguration> findByCreatedBy(User user);
    
    /**
     * Find job configurations by job type
     */
    List<JobConfiguration> findByJobType(String jobType);
    
    /**
     * Find job configurations by name containing a string (case insensitive)
     */
    List<JobConfiguration> findByNameContainingIgnoreCase(String name);
    
    /**
     * Find enabled job configurations by creator
     */
    List<JobConfiguration> findByCreatedByAndEnabledTrue(User user);
    
    /**
     * Find enabled job configurations by job type
     */
    List<JobConfiguration> findByJobTypeAndEnabledTrue(String jobType);
    
    /**
     * Find job configuration by ID and creator
     */
    Optional<JobConfiguration> findByIdAndCreatedBy(String id, User user);
    
    /**
     * Find job configuration by ID and creator with User entity eagerly loaded
     */
    @Query("SELECT jc FROM JobConfiguration jc JOIN FETCH jc.createdBy WHERE jc.id = :id AND jc.createdBy = :user")
    Optional<JobConfiguration> findByIdAndCreatedByWithUser(@Param("id") String id, @Param("user") User user);
    
    /**
     * Find job configurations accessible to a user (created by user)
     * Note: Currently only creator has access, but this can be extended for sharing
     */
    @Query("SELECT jc FROM JobConfiguration jc WHERE jc.createdBy = :user")
    List<JobConfiguration> findAccessibleToUser(@Param("user") User user);
    
    /**
     * Find enabled job configurations accessible to a user
     */
    @Query("SELECT jc FROM JobConfiguration jc WHERE jc.createdBy = :user AND jc.enabled = true")
    List<JobConfiguration> findEnabledAccessibleToUser(@Param("user") User user);
    
    /**
     * Count job configurations by creator
     */
    long countByCreatedBy(User user);
    
    /**
     * Count enabled job configurations by creator
     */
    long countByCreatedByAndEnabledTrue(User user);
} 