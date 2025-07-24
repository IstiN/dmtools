package com.github.istin.dmtools.server.repository;

import com.github.istin.dmtools.auth.model.AuthProvider;
import com.github.istin.dmtools.auth.model.User;
import com.github.istin.dmtools.auth.repository.UserRepository;
import com.github.istin.dmtools.server.model.ExecutionStatus;
import com.github.istin.dmtools.server.model.JobConfiguration;
import com.github.istin.dmtools.server.model.JobExecution;
import com.github.istin.dmtools.server.model.JobExecutionLog;
import com.github.istin.dmtools.server.model.LogLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JobExecutionLogRepository.
 * Tests all query methods and log management functionality.
 */
@DataJpaTest
public class JobExecutionLogRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private JobExecutionLogRepository jobExecutionLogRepository;

    @Autowired
    private JobExecutionRepository jobExecutionRepository;

    @Autowired
    private JobConfigurationRepository jobConfigurationRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private JobConfiguration jobConfig;
    private JobExecution execution1;
    private JobExecution execution2;
    private JobExecutionLog log1;
    private JobExecutionLog log2;
    private JobExecutionLog log3;
    private JobExecutionLog log4;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");
        testUser.setProvider(AuthProvider.GOOGLE);
        testUser.setProviderId("test123");
        testUser = userRepository.save(testUser);

        // Create test job configuration
        jobConfig = new JobConfiguration();
        jobConfig.setName("Test Job");
        jobConfig.setDescription("Test Description");
        jobConfig.setJobType("Expert");
        jobConfig.setCreatedBy(testUser);
        jobConfig.setJobParameters("{\"param1\": \"value1\"}");
        jobConfig.setIntegrationMappings("{\"jira\": \"test-jira\"}");
        jobConfig = jobConfigurationRepository.save(jobConfig);

        // Create test job executions
        execution1 = new JobExecution();
        execution1.setJobConfiguration(jobConfig);
        execution1.setUser(testUser);
        execution1.setStatus(ExecutionStatus.RUNNING);
        execution1.setStartedAt(LocalDateTime.now().minusHours(1));
        execution1.setExecutionParameters("{\"test\": \"params1\"}");
        execution1 = jobExecutionRepository.save(execution1);

        execution2 = new JobExecution();
        execution2.setJobConfiguration(jobConfig);
        execution2.setUser(testUser);
        execution2.setStatus(ExecutionStatus.COMPLETED);
        execution2.setStartedAt(LocalDateTime.now().minusHours(2));
        execution2.setCompletedAt(LocalDateTime.now().minusHours(1));
        execution2.setExecutionParameters("{\"test\": \"params2\"}");
        execution2 = jobExecutionRepository.save(execution2);

        // Create test log entries
        log1 = new JobExecutionLog();
        log1.setExecution(execution1);
        log1.setLevel(LogLevel.INFO);
        log1.setMessage("Starting job execution");
        log1.setTimestamp(LocalDateTime.now().minusMinutes(50));
        log1.setComponent("JobExecutor");
        log1.setThreadName("main");
        log1 = jobExecutionLogRepository.save(log1);

        log2 = new JobExecutionLog();
        log2.setExecution(execution1);
        log2.setLevel(LogLevel.DEBUG);
        log2.setMessage("Processing step 1");
        log2.setTimestamp(LocalDateTime.now().minusMinutes(45));
        log2.setComponent("StepProcessor");
        log2.setThreadName("main");
        log2.setContext("{\"step\": 1}");
        log2 = jobExecutionLogRepository.save(log2);

        log3 = new JobExecutionLog();
        log3.setExecution(execution1);
        log3.setLevel(LogLevel.ERROR);
        log3.setMessage("Error occurred during processing");
        log3.setTimestamp(LocalDateTime.now().minusMinutes(40));
        log3.setComponent("ErrorHandler");
        log3.setThreadName("main");
        log3 = jobExecutionLogRepository.save(log3);

        log4 = new JobExecutionLog();
        log4.setExecution(execution2);
        log4.setLevel(LogLevel.INFO);
        log4.setMessage("Job completed successfully");
        log4.setTimestamp(LocalDateTime.now().minusMinutes(60));
        log4.setComponent("JobExecutor");
        log4.setThreadName("worker-1");
        log4 = jobExecutionLogRepository.save(log4);

        entityManager.flush();
    }

    @Test
    void testFindByExecutionOrderByTimestampAsc() {
        List<JobExecutionLog> logs = jobExecutionLogRepository.findByExecutionOrderByTimestampAsc(execution1);
        
        assertEquals(3, logs.size());
        // Should be ordered by timestamp ASC (oldest first)
        assertEquals(log1.getId(), logs.get(0).getId()); // First log
        assertEquals(log2.getId(), logs.get(1).getId()); // Second log
        assertEquals(log3.getId(), logs.get(2).getId()); // Third log
    }

    @Test
    void testFindByExecutionOrderByTimestampDesc() {
        List<JobExecutionLog> logs = jobExecutionLogRepository.findByExecutionOrderByTimestampDesc(execution1);
        
        assertEquals(3, logs.size());
        // Should be ordered by timestamp DESC (newest first)
        assertEquals(log3.getId(), logs.get(0).getId()); // Latest log
        assertEquals(log2.getId(), logs.get(1).getId()); // Second log
        assertEquals(log1.getId(), logs.get(2).getId()); // First log
    }

    @Test
    void testFindByExecutionOrderByTimestampAscWithPagination() {
        Page<JobExecutionLog> page = jobExecutionLogRepository.findByExecutionOrderByTimestampAsc(execution1, PageRequest.of(0, 2));
        
        assertEquals(2, page.getContent().size());
        assertEquals(3, page.getTotalElements());
        assertEquals(log1.getId(), page.getContent().get(0).getId());
        assertEquals(log2.getId(), page.getContent().get(1).getId());
    }

    @Test
    void testFindByExecutionAndLevelOrderByTimestampAsc() {
        List<JobExecutionLog> infoLogs = jobExecutionLogRepository.findByExecutionAndLevelOrderByTimestampAsc(execution1, LogLevel.INFO);
        
        assertEquals(1, infoLogs.size());
        assertEquals(log1.getId(), infoLogs.get(0).getId());
        
        List<JobExecutionLog> errorLogs = jobExecutionLogRepository.findByExecutionAndLevelOrderByTimestampAsc(execution1, LogLevel.ERROR);
        assertEquals(1, errorLogs.size());
        assertEquals(log3.getId(), errorLogs.get(0).getId());
    }

    @Test
    void testFindByExecutionAndLevelInOrderByTimestampAsc() {
        List<LogLevel> levels = Arrays.asList(LogLevel.INFO, LogLevel.ERROR);
        List<JobExecutionLog> logs = jobExecutionLogRepository.findByExecutionAndLevelInOrderByTimestampAsc(execution1, levels);
        
        assertEquals(2, logs.size());
        assertEquals(log1.getId(), logs.get(0).getId()); // INFO log
        assertEquals(log3.getId(), logs.get(1).getId()); // ERROR log
    }

    @Test
    void testFindByExecutionAndTimeRange() {
        LocalDateTime startTime = LocalDateTime.now().minusMinutes(55);
        LocalDateTime endTime = LocalDateTime.now().minusMinutes(35);
        
        List<JobExecutionLog> logs = jobExecutionLogRepository.findByExecutionAndTimeRange(execution1, startTime, endTime);
        
        assertEquals(3, logs.size()); // All logs for execution1 should be in this range
    }

    @Test
    void testCountByLevelForExecution() {
        List<Object[]> levelCounts = jobExecutionLogRepository.countByLevelForExecution(execution1);
        
        assertNotNull(levelCounts);
        assertEquals(3, levelCounts.size()); // INFO, DEBUG, ERROR levels
        
        // Verify counts
        for (Object[] row : levelCounts) {
            LogLevel level = (LogLevel) row[0];
            Long count = (Long) row[1];
            
            assertEquals(1L, count); // Each level has 1 log entry
            assertTrue(Arrays.asList(LogLevel.INFO, LogLevel.DEBUG, LogLevel.ERROR).contains(level));
        }
    }

    @Test
    void testCountByExecution() {
        long execution1LogCount = jobExecutionLogRepository.countByExecution(execution1);
        assertEquals(3L, execution1LogCount);
        
        long execution2LogCount = jobExecutionLogRepository.countByExecution(execution2);
        assertEquals(1L, execution2LogCount);
    }

    @Test
    void testCountByExecutionAndLevel() {
        long infoCount = jobExecutionLogRepository.countByExecutionAndLevel(execution1, LogLevel.INFO);
        assertEquals(1L, infoCount);
        
        long debugCount = jobExecutionLogRepository.countByExecutionAndLevel(execution1, LogLevel.DEBUG);
        assertEquals(1L, debugCount);
        
        long warnCount = jobExecutionLogRepository.countByExecutionAndLevel(execution1, LogLevel.WARN);
        assertEquals(0L, warnCount); // No WARN logs
    }

    @Test
    void testFindByExecutionAndComponentOrderByTimestampAsc() {
        List<JobExecutionLog> executorLogs = jobExecutionLogRepository.findByExecutionAndComponentOrderByTimestampAsc(execution1, "JobExecutor");
        
        assertEquals(1, executorLogs.size());
        assertEquals(log1.getId(), executorLogs.get(0).getId());
        
        List<JobExecutionLog> processorLogs = jobExecutionLogRepository.findByExecutionAndComponentOrderByTimestampAsc(execution1, "StepProcessor");
        assertEquals(1, processorLogs.size());
        assertEquals(log2.getId(), processorLogs.get(0).getId());
    }

    @Test
    void testFindByExecutionAndMessageContaining() {
        List<JobExecutionLog> logs = jobExecutionLogRepository.findByExecutionAndMessageContaining(execution1, "job");
        
        assertEquals(1, logs.size());
        assertEquals(log1.getId(), logs.get(0).getId()); // "Starting job execution"
        
        List<JobExecutionLog> errorLogs = jobExecutionLogRepository.findByExecutionAndMessageContaining(execution1, "error");
        assertEquals(1, errorLogs.size());
        assertEquals(log3.getId(), errorLogs.get(0).getId());
    }

    @Test
    void testFindByExecutionIdOrderByTimestampAsc() {
        List<JobExecutionLog> logs = jobExecutionLogRepository.findByExecutionIdOrderByTimestampAsc(execution1.getId());
        
        assertEquals(3, logs.size());
        assertEquals(log1.getId(), logs.get(0).getId());
        assertEquals(log2.getId(), logs.get(1).getId());
        assertEquals(log3.getId(), logs.get(2).getId());
    }

    @Test
    void testFindRecentLogsForExecution() {
        Page<JobExecutionLog> recentLogs = jobExecutionLogRepository.findRecentLogsForExecution(execution1, PageRequest.of(0, 2));
        
        assertEquals(2, recentLogs.getContent().size());
        assertEquals(3, recentLogs.getTotalElements());
        // Should be ordered by timestamp DESC (newest first)
        assertEquals(log3.getId(), recentLogs.getContent().get(0).getId());
        assertEquals(log2.getId(), recentLogs.getContent().get(1).getId());
    }

    @Test
    void testGetLogStatisticsForExecution() {
        Object[] stats = jobExecutionLogRepository.getLogStatisticsForExecution(execution1);
        
        assertNotNull(stats);
        assertEquals(6, stats.length); // total, errors, warnings, infos, debugs
        
        assertEquals(3L, stats[0]); // total logs
        assertEquals(1L, stats[1]); // error count
        assertEquals(0L, stats[2]); // warning count
        assertEquals(1L, stats[3]); // info count
        assertEquals(1L, stats[4]); // debug count
    }

    @Test
    void testJobExecutionLogConstructors() {
        // Test basic constructor
        JobExecutionLog basicLog = new JobExecutionLog(execution1, LogLevel.INFO, "Test message");
        assertEquals(execution1, basicLog.getExecution());
        assertEquals(LogLevel.INFO, basicLog.getLevel());
        assertEquals("Test message", basicLog.getMessage());
        assertNotNull(basicLog.getTimestamp());
        assertNotNull(basicLog.getThreadName());

        // Test constructor with component
        JobExecutionLog componentLog = new JobExecutionLog(execution1, LogLevel.WARN, "Warning message", "TestComponent");
        assertEquals("TestComponent", componentLog.getComponent());

        // Test constructor with context
        JobExecutionLog contextLog = new JobExecutionLog(execution1, LogLevel.ERROR, "Error message", "ErrorComponent", "{\"error\": \"details\"}");
        assertEquals("{\"error\": \"details\"}", contextLog.getContext());
    }

    @Test
    void testJobExecutionLogHelperMethods() {
        assertTrue(log3.isError());
        assertFalse(log1.isError());
        
        assertFalse(log2.isWarning());
        // Create a warning log to test
        JobExecutionLog warnLog = new JobExecutionLog(execution1, LogLevel.WARN, "Warning");
        assertTrue(warnLog.isWarning());
        
        // Test formatted string
        String formatted = log1.toFormattedString();
        assertTrue(formatted.contains(log1.getTimestamp().toString()));
        assertTrue(formatted.contains("INFO"));
        assertTrue(formatted.contains("JobExecutor"));
        assertTrue(formatted.contains("Starting job execution"));
    }

    @Test
    void testJobExecutionLogPrePersist() {
        JobExecutionLog newLog = new JobExecutionLog();
        newLog.setExecution(execution1);
        newLog.setLevel(LogLevel.INFO);
        newLog.setMessage("New test log");
        
        // Timestamp and threadName should be set by @PrePersist
        JobExecutionLog saved = jobExecutionLogRepository.save(newLog);
        
        assertNotNull(saved.getTimestamp());
        assertNotNull(saved.getThreadName());
    }

    @Test
    void testDeleteByExecution() {
        // Verify logs exist
        assertEquals(3L, jobExecutionLogRepository.countByExecution(execution1));
        
        // Delete logs for execution1
        jobExecutionLogRepository.deleteByExecution(execution1);
        entityManager.flush();
        
        // Verify logs are deleted
        assertEquals(0L, jobExecutionLogRepository.countByExecution(execution1));
        // execution2 logs should still exist
        assertEquals(1L, jobExecutionLogRepository.countByExecution(execution2));
    }

    @Test
    void testDeleteByExecutionId() {
        // Verify logs exist
        assertEquals(1L, jobExecutionLogRepository.countByExecution(execution2));
        
        // Delete logs for execution2 by ID
        int deletedCount = jobExecutionLogRepository.deleteByExecutionId(execution2.getId());
        entityManager.flush();
        
        assertEquals(1, deletedCount);
        assertEquals(0L, jobExecutionLogRepository.countByExecution(execution2));
        // execution1 logs should still exist
        assertEquals(3L, jobExecutionLogRepository.countByExecution(execution1));
    }
} 