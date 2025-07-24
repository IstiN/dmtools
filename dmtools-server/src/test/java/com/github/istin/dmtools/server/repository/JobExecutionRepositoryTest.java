package com.github.istin.dmtools.server.repository;

import com.github.istin.dmtools.auth.model.AuthProvider;
import com.github.istin.dmtools.auth.model.User;
import com.github.istin.dmtools.auth.repository.UserRepository;
import com.github.istin.dmtools.server.model.ExecutionStatus;
import com.github.istin.dmtools.server.model.JobConfiguration;
import com.github.istin.dmtools.server.model.JobExecution;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JobExecutionRepository.
 * Tests all query methods and business logic.
 */
@DataJpaTest
public class JobExecutionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private JobExecutionRepository jobExecutionRepository;

    @Autowired
    private JobConfigurationRepository jobConfigurationRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private User anotherUser;
    private JobConfiguration jobConfig1;
    private JobConfiguration jobConfig2;
    private JobExecution execution1;
    private JobExecution execution2;
    private JobExecution execution3;

    @BeforeEach
    void setUp() {
        // Create test users
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");
        testUser.setProvider(AuthProvider.GOOGLE);
        testUser.setProviderId("test123");
        testUser = userRepository.save(testUser);

        anotherUser = new User();
        anotherUser.setEmail("another@example.com");
        anotherUser.setName("Another User");
        anotherUser.setProvider(AuthProvider.GOOGLE);
        anotherUser.setProviderId("another123");
        anotherUser = userRepository.save(anotherUser);

        // Create test job configurations
        jobConfig1 = new JobConfiguration();
        jobConfig1.setName("Test Job 1");
        jobConfig1.setDescription("Test Description 1");
        jobConfig1.setJobType("Expert");
        jobConfig1.setCreatedBy(testUser);
        jobConfig1.setJobParameters("{\"param1\": \"value1\"}");
        jobConfig1.setIntegrationMappings("{\"jira\": \"test-jira\"}");
        jobConfig1 = jobConfigurationRepository.save(jobConfig1);

        jobConfig2 = new JobConfiguration();
        jobConfig2.setName("Test Job 2");
        jobConfig2.setDescription("Test Description 2");
        jobConfig2.setJobType("TestCasesGenerator");
        jobConfig2.setCreatedBy(anotherUser);
        jobConfig2.setJobParameters("{\"param2\": \"value2\"}");
        jobConfig2.setIntegrationMappings("{\"confluence\": \"test-confluence\"}");
        jobConfig2 = jobConfigurationRepository.save(jobConfig2);

        // Create test job executions
        execution1 = new JobExecution();
        execution1.setJobConfiguration(jobConfig1);
        execution1.setUser(testUser);
        execution1.setStatus(ExecutionStatus.COMPLETED);
        execution1.setStartedAt(LocalDateTime.now().minusHours(2));
        execution1.setCompletedAt(LocalDateTime.now().minusHours(1));
        execution1.setExecutionParameters("{\"test\": \"params1\"}");
        execution1.setResultSummary("Job completed successfully");
        execution1 = jobExecutionRepository.save(execution1);

        execution2 = new JobExecution();
        execution2.setJobConfiguration(jobConfig1);
        execution2.setUser(testUser);
        execution2.setStatus(ExecutionStatus.RUNNING);
        execution2.setStartedAt(LocalDateTime.now().minusMinutes(30));
        execution2.setExecutionParameters("{\"test\": \"params2\"}");
        execution2.setThreadName("test-thread-1");
        execution2 = jobExecutionRepository.save(execution2);

        execution3 = new JobExecution();
        execution3.setJobConfiguration(jobConfig2);
        execution3.setUser(anotherUser);
        execution3.setStatus(ExecutionStatus.FAILED);
        execution3.setStartedAt(LocalDateTime.now().minusHours(3));
        execution3.setCompletedAt(LocalDateTime.now().minusHours(2));
        execution3.setExecutionParameters("{\"test\": \"params3\"}");
        execution3.setErrorMessage("Job failed with error");
        execution3 = jobExecutionRepository.save(execution3);

        entityManager.flush();
    }

    @Test
    void testFindByUserOrderByStartedAtDesc() {
        List<JobExecution> executions = jobExecutionRepository.findByUserOrderByStartedAtDesc(testUser);
        
        assertEquals(2, executions.size());
        // Should be ordered by startedAt DESC (newest first)
        assertEquals(execution2.getId(), executions.get(0).getId()); // Most recent
        assertEquals(execution1.getId(), executions.get(1).getId()); // Older
    }

    @Test
    void testFindByUserOrderByStartedAtDescWithPagination() {
        Page<JobExecution> page = jobExecutionRepository.findByUserOrderByStartedAtDesc(testUser, PageRequest.of(0, 1));
        
        assertEquals(1, page.getContent().size());
        assertEquals(2, page.getTotalElements());
        assertEquals(execution2.getId(), page.getContent().get(0).getId()); // Most recent
    }

    @Test
    void testFindByJobConfigurationOrderByStartedAtDesc() {
        List<JobExecution> executions = jobExecutionRepository.findByJobConfigurationOrderByStartedAtDesc(jobConfig1);
        
        assertEquals(2, executions.size());
        assertEquals(execution2.getId(), executions.get(0).getId()); // Most recent
        assertEquals(execution1.getId(), executions.get(1).getId()); // Older
    }

    @Test
    void testFindByStatusIn() {
        List<ExecutionStatus> activeStatuses = Arrays.asList(ExecutionStatus.PENDING, ExecutionStatus.RUNNING);
        List<JobExecution> activeExecutions = jobExecutionRepository.findByStatusIn(activeStatuses);
        
        assertEquals(1, activeExecutions.size());
        assertEquals(execution2.getId(), activeExecutions.get(0).getId());
        assertEquals(ExecutionStatus.RUNNING, activeExecutions.get(0).getStatus());
    }

    @Test
    void testFindByStatus() {
        List<JobExecution> runningExecutions = jobExecutionRepository.findByStatus(ExecutionStatus.RUNNING);
        
        assertEquals(1, runningExecutions.size());
        assertEquals(execution2.getId(), runningExecutions.get(0).getId());
    }

    @Test
    void testFindByUserAndStatus() {
        List<JobExecution> userRunningExecutions = jobExecutionRepository.findByUserAndStatus(testUser, ExecutionStatus.RUNNING);
        
        assertEquals(1, userRunningExecutions.size());
        assertEquals(execution2.getId(), userRunningExecutions.get(0).getId());
        
        List<JobExecution> userFailedExecutions = jobExecutionRepository.findByUserAndStatus(testUser, ExecutionStatus.FAILED);
        assertEquals(0, userFailedExecutions.size()); // execution3 belongs to anotherUser
    }

    @Test
    void testFindByUserAndDateRange() {
        LocalDateTime startDate = LocalDateTime.now().minusHours(4);
        LocalDateTime endDate = LocalDateTime.now();
        
        List<JobExecution> executions = jobExecutionRepository.findByUserAndDateRange(testUser, startDate, endDate);
        
        assertEquals(2, executions.size());
        // Should include both execution1 and execution2 for testUser
    }

    @Test
    void testCountByStatusForUser() {
        List<Object[]> statusCounts = jobExecutionRepository.countByStatusForUser(testUser);
        
        assertNotNull(statusCounts);
        assertEquals(2, statusCounts.size()); // COMPLETED and RUNNING statuses
        
        // Verify counts
        for (Object[] row : statusCounts) {
            ExecutionStatus status = (ExecutionStatus) row[0];
            Long count = (Long) row[1];
            
            if (status == ExecutionStatus.COMPLETED) {
                assertEquals(1L, count);
            } else if (status == ExecutionStatus.RUNNING) {
                assertEquals(1L, count);
            }
        }
    }

    @Test
    void testCountByUser() {
        long userExecutionCount = jobExecutionRepository.countByUser(testUser);
        assertEquals(2L, userExecutionCount);
        
        long anotherUserExecutionCount = jobExecutionRepository.countByUser(anotherUser);
        assertEquals(1L, anotherUserExecutionCount);
    }

    @Test
    void testCountByUserAndStatus() {
        long runningCount = jobExecutionRepository.countByUserAndStatus(testUser, ExecutionStatus.RUNNING);
        assertEquals(1L, runningCount);
        
        long completedCount = jobExecutionRepository.countByUserAndStatus(testUser, ExecutionStatus.COMPLETED);
        assertEquals(1L, completedCount);
        
        long failedCount = jobExecutionRepository.countByUserAndStatus(testUser, ExecutionStatus.FAILED);
        assertEquals(0L, failedCount);
    }

    @Test
    void testFindByIdAndUser() {
        Optional<JobExecution> found = jobExecutionRepository.findByIdAndUser(execution1.getId(), testUser);
        assertTrue(found.isPresent());
        assertEquals(execution1.getId(), found.get().getId());
        
        // Test security - should not find execution3 for testUser (belongs to anotherUser)
        Optional<JobExecution> notFound = jobExecutionRepository.findByIdAndUser(execution3.getId(), testUser);
        assertFalse(notFound.isPresent());
    }

    @Test
    void testFindActiveExecutionsForUser() {
        List<JobExecution> activeExecutions = jobExecutionRepository.findActiveExecutionsForUser(testUser);
        
        assertEquals(1, activeExecutions.size());
        assertEquals(execution2.getId(), activeExecutions.get(0).getId());
        assertEquals(ExecutionStatus.RUNNING, activeExecutions.get(0).getStatus());
    }

    @Test
    void testJobExecutionEntityMethods() {
        // Test helper methods
        assertTrue(execution2.isActive());
        assertFalse(execution1.isActive());
        
        assertTrue(execution1.isCompleted());
        assertFalse(execution2.isCompleted());
        
        // Test duration calculation
        assertNotNull(execution1.getDurationMillis());
        assertTrue(execution1.getDurationMillis() > 0);
        assertNull(execution2.getDurationMillis()); // Not completed yet
        
        // Test status transition methods
        JobExecution testExecution = new JobExecution();
        testExecution.markAsRunning();
        assertEquals(ExecutionStatus.RUNNING, testExecution.getStatus());
        assertNotNull(testExecution.getThreadName());
        
        testExecution.markAsCompleted("Test completed");
        assertEquals(ExecutionStatus.COMPLETED, testExecution.getStatus());
        assertEquals("Test completed", testExecution.getResultSummary());
        assertNotNull(testExecution.getCompletedAt());
        
        JobExecution failedExecution = new JobExecution();
        failedExecution.markAsFailed("Test error");
        assertEquals(ExecutionStatus.FAILED, failedExecution.getStatus());
        assertEquals("Test error", failedExecution.getErrorMessage());
        assertNotNull(failedExecution.getCompletedAt());
        
        JobExecution cancelledExecution = new JobExecution();
        cancelledExecution.markAsCancelled();
        assertEquals(ExecutionStatus.CANCELLED, cancelledExecution.getStatus());
        assertNotNull(cancelledExecution.getCompletedAt());
    }

    @Test
    void testJobExecutionPrePersist() {
        JobExecution newExecution = new JobExecution();
        newExecution.setJobConfiguration(jobConfig1);
        newExecution.setUser(testUser);
        newExecution.setExecutionParameters("{\"test\": \"new\"}");
        
        // Status and startedAt should be set by @PrePersist
        JobExecution saved = jobExecutionRepository.save(newExecution);
        
        assertNotNull(saved.getStartedAt());
        assertEquals(ExecutionStatus.PENDING, saved.getStatus());
    }
} 