package com.github.istin.dmtools.server.repository;

import com.github.istin.dmtools.auth.model.User;
import com.github.istin.dmtools.server.model.ExecutionStatus;
import com.github.istin.dmtools.server.model.JobConfiguration;
import com.github.istin.dmtools.server.model.JobExecution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for JobExecution entity.
 * Provides methods for querying job executions by various criteria.
 */
@Repository
public interface JobExecutionRepository extends JpaRepository<JobExecution, String> {
    
    /**
     * Find executions by user ordered by start time (most recent first).
     */
    List<JobExecution> findByUserOrderByStartedAtDesc(User user);
    
    /**
     * Find executions by user with pagination.
     */
    Page<JobExecution> findByUserOrderByStartedAtDesc(User user, Pageable pageable);
    
    /**
     * Find executions by job configuration ordered by start time (most recent first).
     */
    List<JobExecution> findByJobConfigurationOrderByStartedAtDesc(JobConfiguration jobConfig);
    
    /**
     * Find executions by job configuration with pagination.
     */
    Page<JobExecution> findByJobConfigurationOrderByStartedAtDesc(JobConfiguration jobConfig, Pageable pageable);
    
    /**
     * Find running executions (by status).
     */
    List<JobExecution> findByStatusIn(List<ExecutionStatus> statuses);
    
    /**
     * Find executions by status.
     */
    List<JobExecution> findByStatus(ExecutionStatus status);
    
    /**
     * Find executions by user and status.
     */
    List<JobExecution> findByUserAndStatus(User user, ExecutionStatus status);
    
    /**
     * Find executions by user and status with pagination.
     */
    Page<JobExecution> findByUserAndStatus(User user, ExecutionStatus status, Pageable pageable);
    
    /**
     * Find executions in date range for a user.
     */
    @Query("SELECT e FROM JobExecution e WHERE e.user = :user AND e.startedAt BETWEEN :startDate AND :endDate ORDER BY e.startedAt DESC")
    List<JobExecution> findByUserAndDateRange(@Param("user") User user, 
                                              @Param("startDate") LocalDateTime startDate, 
                                              @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find executions in date range for a user with pagination.
     */
    @Query("SELECT e FROM JobExecution e WHERE e.user = :user AND e.startedAt BETWEEN :startDate AND :endDate ORDER BY e.startedAt DESC")
    Page<JobExecution> findByUserAndDateRange(@Param("user") User user, 
                                              @Param("startDate") LocalDateTime startDate, 
                                              @Param("endDate") LocalDateTime endDate,
                                              Pageable pageable);
    
    /**
     * Count executions by status for user.
     */
    @Query("SELECT e.status, COUNT(e) FROM JobExecution e WHERE e.user = :user GROUP BY e.status")
    List<Object[]> countByStatusForUser(@Param("user") User user);
    
    /**
     * Count total executions for user.
     */
    long countByUser(User user);
    
    /**
     * Count executions by user and status.
     */
    long countByUserAndStatus(User user, ExecutionStatus status);
    
    /**
     * Find execution by ID and user (for security).
     */
    Optional<JobExecution> findByIdAndUser(String id, User user);
    
    /**
     * Find active executions for user (PENDING or RUNNING).
     */
    @Query("SELECT e FROM JobExecution e WHERE e.user = :user AND e.status IN ('PENDING', 'RUNNING') ORDER BY e.startedAt DESC")
    List<JobExecution> findActiveExecutionsForUser(@Param("user") User user);
    
    /**
     * Find recent executions for user (last N executions).
     */
    @Query("SELECT e FROM JobExecution e WHERE e.user = :user ORDER BY e.startedAt DESC")
    Page<JobExecution> findRecentExecutionsForUser(@Param("user") User user, Pageable pageable);
    
    /**
     * Find executions by job configuration and user.
     */
    List<JobExecution> findByJobConfigurationAndUser(JobConfiguration jobConfig, User user);
    
    /**
     * Find executions by job configuration and user with pagination.
     */
    Page<JobExecution> findByJobConfigurationAndUser(JobConfiguration jobConfig, User user, Pageable pageable);
    
    /**
     * Calculate average execution duration for user in minutes.
     */
    @Query("SELECT AVG(EXTRACT(EPOCH FROM (e.completedAt - e.startedAt)) / 60.0) FROM JobExecution e WHERE e.user = :user AND e.completedAt IS NOT NULL")
    Double getAverageExecutionDurationMinutesForUser(@Param("user") User user);
    
    /**
     * Get execution success rate for user (percentage).
     */
    @Query("SELECT CAST(COUNT(CASE WHEN e.status = 'COMPLETED' THEN 1 END) AS DOUBLE) * 100.0 / COUNT(*) FROM JobExecution e WHERE e.user = :user")
    Double getSuccessRateForUser(@Param("user") User user);
    
    /**
     * Find executions that have been running for too long (stale executions).
     * Useful for cleanup and monitoring.
     */
    @Query("SELECT e FROM JobExecution e WHERE e.status = 'RUNNING' AND e.startedAt < :cutoffTime")
    List<JobExecution> findStaleRunningExecutions(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * Delete old completed executions (for cleanup).
     */
    @Query("DELETE FROM JobExecution e WHERE e.status IN ('COMPLETED', 'FAILED', 'CANCELLED') AND e.completedAt < :cutoffDate")
    int deleteOldExecutions(@Param("cutoffDate") LocalDateTime cutoffDate);
} 