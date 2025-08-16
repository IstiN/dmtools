package com.github.istin.dmtools.server.service;

import com.github.istin.dmtools.auth.model.User;
import com.github.istin.dmtools.auth.repository.UserRepository;
import com.github.istin.dmtools.server.exception.JobConfigurationDeletionException;
import com.github.istin.dmtools.server.model.JobConfiguration;
import com.github.istin.dmtools.server.model.JobExecution;
import com.github.istin.dmtools.server.model.ExecutionStatus;
import com.github.istin.dmtools.server.repository.JobConfigurationRepository;
import com.github.istin.dmtools.server.repository.JobExecutionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobConfigurationServiceTest {

    @Mock
    private JobConfigurationRepository jobConfigRepository;
    @Mock
    private JobExecutionRepository jobExecutionRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JobConfigurationService jobConfigurationService;

    private User testUser;
    private JobConfiguration testJobConfig;
    private String testJobConfigId = "test-job-config-id";
    private String testUserId = "test-user-id";

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(testUserId);
        testUser.setEmail("test@example.com");

        testJobConfig = new JobConfiguration();
        testJobConfig.setId(testJobConfigId);
        testJobConfig.setName("Test Job Config");
        testJobConfig.setCreatedBy(testUser);

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(jobConfigRepository.findByIdAndCreatedBy(testJobConfigId, testUser)).thenReturn(Optional.of(testJobConfig));
    }

    @Test
    void deleteJobConfiguration_success_noExecutions() {
        when(jobExecutionRepository.findByJobConfigurationOrderByStartedAtDesc(testJobConfig)).thenReturn(Collections.emptyList());

        boolean deleted = jobConfigurationService.deleteJobConfiguration(testJobConfigId, testUserId);

        assertTrue(deleted);
        verify(jobExecutionRepository, never()).deleteAll(anyList());
        verify(jobConfigRepository, times(1)).delete(testJobConfig);
    }

    @Test
    void deleteJobConfiguration_success_withCompletedExecutions() {
        JobExecution completedExecution1 = new JobExecution();
        completedExecution1.setStatus(ExecutionStatus.COMPLETED);
        JobExecution completedExecution2 = new JobExecution();
        completedExecution2.setStatus(ExecutionStatus.FAILED);
        List<JobExecution> completedExecutions = Arrays.asList(completedExecution1, completedExecution2);

        when(jobExecutionRepository.findByJobConfigurationOrderByStartedAtDesc(testJobConfig)).thenReturn(completedExecutions);

        boolean deleted = jobConfigurationService.deleteJobConfiguration(testJobConfigId, testUserId);

        assertTrue(deleted);
        verify(jobExecutionRepository, times(1)).deleteAll(completedExecutions);
        verify(jobConfigRepository, times(1)).delete(testJobConfig);
    }

    @Test
    void deleteJobConfiguration_throwsException_withActiveExecutions() {
        JobExecution activeExecution = new JobExecution();
        activeExecution.setStatus(ExecutionStatus.RUNNING);
        List<JobExecution> activeExecutions = Collections.singletonList(activeExecution);

        when(jobExecutionRepository.findByJobConfigurationOrderByStartedAtDesc(testJobConfig)).thenReturn(activeExecutions);

        JobConfigurationDeletionException exception = assertThrows(JobConfigurationDeletionException.class, () -> {
            jobConfigurationService.deleteJobConfiguration(testJobConfigId, testUserId);
        });

        assertEquals("Cannot delete job configuration with active executions. Please wait for them to complete or cancel them.", exception.getMessage());
        verify(jobExecutionRepository, never()).deleteAll(anyList());
        verify(jobConfigRepository, never()).delete(any(JobConfiguration.class));
    }

    @Test
    void deleteJobConfiguration_notFound() {
        when(jobConfigRepository.findByIdAndCreatedBy(testJobConfigId, testUser)).thenReturn(Optional.empty());

        boolean deleted = jobConfigurationService.deleteJobConfiguration(testJobConfigId, testUserId);

        assertFalse(deleted);
        verify(jobExecutionRepository, never()).findByJobConfigurationOrderByStartedAtDesc(any(JobConfiguration.class));
        verify(jobConfigRepository, never()).delete(any(JobConfiguration.class));
    }

    @Test
    void deleteJobConfiguration_userNotFound() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            jobConfigurationService.deleteJobConfiguration(testJobConfigId, testUserId);
        });

        assertEquals("User not found", exception.getMessage());
        verify(jobConfigRepository, never()).findByIdAndCreatedBy(anyString(), any(User.class));
        verify(jobExecutionRepository, never()).findByJobConfigurationOrderByStartedAtDesc(any(JobConfiguration.class));
        verify(jobConfigRepository, never()).delete(any(JobConfiguration.class));
    }
}
