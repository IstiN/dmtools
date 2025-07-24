package com.github.istin.dmtools.server.repository;

import com.github.istin.dmtools.server.model.JobExecution;
import com.github.istin.dmtools.server.model.JobExecutionLog;
import com.github.istin.dmtools.server.model.LogLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for JobExecutionLog entity.
 * Provides methods for querying and managing job execution log entries.
 */
@Repository
public interface JobExecutionLogRepository extends JpaRepository<JobExecutionLog, Long> {
    
    /**
     * Find logs for execution ordered by timestamp (oldest first).
     */
    List<JobExecutionLog> findByExecutionOrderByTimestampAsc(JobExecution execution);
    
    /**
     * Find logs for execution with pagination, ordered by timestamp.
     */
    Page<JobExecutionLog> findByExecutionOrderByTimestampAsc(JobExecution execution, Pageable pageable);
    
    /**
     * Find logs for execution ordered by timestamp (newest first).
     */
    List<JobExecutionLog> findByExecutionOrderByTimestampDesc(JobExecution execution);
    
    /**
     * Find logs for execution with pagination, ordered by timestamp (newest first).
     */
    Page<JobExecutionLog> findByExecutionOrderByTimestampDesc(JobExecution execution, Pageable pageable);
    
    /**
     * Find logs by execution and level ordered by timestamp.
     */
    List<JobExecutionLog> findByExecutionAndLevelOrderByTimestampAsc(JobExecution execution, LogLevel level);
    
    /**
     * Find logs by execution and level with pagination.
     */
    Page<JobExecutionLog> findByExecutionAndLevelOrderByTimestampAsc(JobExecution execution, LogLevel level, Pageable pageable);
    
    /**
     * Find logs by execution and multiple levels.
     */
    List<JobExecutionLog> findByExecutionAndLevelInOrderByTimestampAsc(JobExecution execution, List<LogLevel> levels);
    
    /**
     * Find logs by execution and multiple levels with pagination.
     */
    Page<JobExecutionLog> findByExecutionAndLevelInOrderByTimestampAsc(JobExecution execution, List<LogLevel> levels, Pageable pageable);
    
    /**
     * Find logs in time range for an execution.
     */
    @Query("SELECT l FROM JobExecutionLog l WHERE l.execution = :execution AND l.timestamp BETWEEN :startTime AND :endTime ORDER BY l.timestamp ASC")
    List<JobExecutionLog> findByExecutionAndTimeRange(@Param("execution") JobExecution execution,
                                                       @Param("startTime") LocalDateTime startTime,
                                                       @Param("endTime") LocalDateTime endTime);
    
    /**
     * Find logs in time range for an execution with pagination.
     */
    @Query("SELECT l FROM JobExecutionLog l WHERE l.execution = :execution AND l.timestamp BETWEEN :startTime AND :endTime ORDER BY l.timestamp ASC")
    Page<JobExecutionLog> findByExecutionAndTimeRange(@Param("execution") JobExecution execution,
                                                       @Param("startTime") LocalDateTime startTime,
                                                       @Param("endTime") LocalDateTime endTime,
                                                       Pageable pageable);
    
    /**
     * Count logs by level for execution.
     */
    @Query("SELECT l.level, COUNT(l) FROM JobExecutionLog l WHERE l.execution = :execution GROUP BY l.level")
    List<Object[]> countByLevelForExecution(@Param("execution") JobExecution execution);
    
    /**
     * Count total logs for execution.
     */
    long countByExecution(JobExecution execution);
    
    /**
     * Count logs by execution and level.
     */
    long countByExecutionAndLevel(JobExecution execution, LogLevel level);
    
    /**
     * Find error logs for execution.
     */
    List<JobExecutionLog> findByExecutionAndLevelOrderByTimestampDesc(JobExecution execution, LogLevel level);
    
    /**
     * Find logs by component name.
     */
    List<JobExecutionLog> findByExecutionAndComponentOrderByTimestampAsc(JobExecution execution, String component);
    
    /**
     * Find logs by component name with pagination.
     */
    Page<JobExecutionLog> findByExecutionAndComponentOrderByTimestampAsc(JobExecution execution, String component, Pageable pageable);
    
    /**
     * Find logs containing specific text in message.
     */
    @Query("SELECT l FROM JobExecutionLog l WHERE l.execution = :execution AND LOWER(l.message) LIKE LOWER(CONCAT('%', :searchText, '%')) ORDER BY l.timestamp ASC")
    List<JobExecutionLog> findByExecutionAndMessageContaining(@Param("execution") JobExecution execution, 
                                                               @Param("searchText") String searchText);
    
    /**
     * Find logs containing specific text in message with pagination.
     */
    @Query("SELECT l FROM JobExecutionLog l WHERE l.execution = :execution AND LOWER(l.message) LIKE LOWER(CONCAT('%', :searchText, '%')) ORDER BY l.timestamp ASC")
    Page<JobExecutionLog> findByExecutionAndMessageContaining(@Param("execution") JobExecution execution, 
                                                               @Param("searchText") String searchText,
                                                               Pageable pageable);
    
    /**
     * Find logs by execution ID (for cases where we have execution ID but not entity).
     */
    @Query("SELECT l FROM JobExecutionLog l WHERE l.execution.id = :executionId ORDER BY l.timestamp ASC")
    List<JobExecutionLog> findByExecutionIdOrderByTimestampAsc(@Param("executionId") String executionId);
    
    /**
     * Find logs by execution ID with pagination.
     */
    @Query("SELECT l FROM JobExecutionLog l WHERE l.execution.id = :executionId ORDER BY l.timestamp ASC")
    Page<JobExecutionLog> findByExecutionIdOrderByTimestampAsc(@Param("executionId") String executionId, Pageable pageable);
    
    /**
     * Find recent logs for execution (last N logs).
     */
    @Query("SELECT l FROM JobExecutionLog l WHERE l.execution = :execution ORDER BY l.timestamp DESC")
    Page<JobExecutionLog> findRecentLogsForExecution(@Param("execution") JobExecution execution, Pageable pageable);
    
    /**
     * Get first log entry for execution (earliest timestamp).
     */
    @Query("SELECT l FROM JobExecutionLog l WHERE l.execution = :execution ORDER BY l.timestamp ASC LIMIT 1")
    JobExecutionLog findFirstLogForExecution(@Param("execution") JobExecution execution);
    
    /**
     * Get last log entry for execution (latest timestamp).
     */
    @Query("SELECT l FROM JobExecutionLog l WHERE l.execution = :execution ORDER BY l.timestamp DESC LIMIT 1")
    JobExecutionLog findLastLogForExecution(@Param("execution") JobExecution execution);
    
    /**
     * Delete old logs (for cleanup jobs).
     * Deletes logs older than the specified cutoff date.
     */
    @Modifying
    @Query("DELETE FROM JobExecutionLog l WHERE l.timestamp < :cutoffDate")
    int deleteLogsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Delete logs for specific execution.
     */
    @Modifying
    void deleteByExecution(JobExecution execution);
    
    /**
     * Delete logs for specific execution by ID.
     */
    @Modifying
    @Query("DELETE FROM JobExecutionLog l WHERE l.execution.id = :executionId")
    int deleteByExecutionId(@Param("executionId") String executionId);
    
    /**
     * Get log statistics for execution.
     * Returns array with [total_logs, error_count, warn_count, info_count, debug_count]
     */
    @Query("SELECT " +
           "COUNT(*) as total, " +
           "SUM(CASE WHEN l.level = 'ERROR' THEN 1 ELSE 0 END) as errors, " +
           "SUM(CASE WHEN l.level = 'WARN' THEN 1 ELSE 0 END) as warnings, " +
           "SUM(CASE WHEN l.level = 'INFO' THEN 1 ELSE 0 END) as infos, " +
           "SUM(CASE WHEN l.level = 'DEBUG' THEN 1 ELSE 0 END) as debugs " +
           "FROM JobExecutionLog l WHERE l.execution = :execution")
    Object[] getLogStatisticsForExecution(@Param("execution") JobExecution execution);
} 