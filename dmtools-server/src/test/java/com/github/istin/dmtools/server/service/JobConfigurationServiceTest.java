package com.github.istin.dmtools.server.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.istin.dmtools.auth.model.User;
import com.github.istin.dmtools.auth.repository.UserRepository;
import com.github.istin.dmtools.server.exception.JobConfigurationDeletionException;
import com.github.istin.dmtools.server.model.ExecutionStatus;
import com.github.istin.dmtools.server.model.JobConfiguration;
import com.github.istin.dmtools.server.model.JobExecution;
import com.github.istin.dmtools.server.repository.JobConfigurationRepository;
import com.github.istin.dmtools.server.repository.JobExecutionLogRepository;
import com.github.istin.dmtools.server.repository.JobExecutionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JobConfigurationServiceTest {

    @Mock
    private JobConfigurationRepository jobConfigRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JobExecutionRepository jobExecutionRepository;
    @Mock
    private JobExecutionLogRepository jobExecutionLogRepository;
    @Mock
    private ObjectMapper objectMapper; // Mocked as it's a dependency, but not directly used in delete method logic
    @Mock
    private DotNotationTransformer dotNotationTransformer; // Mocked
    @Mock
    private ParameterValidator parameterValidator; // Mocked

    @InjectMocks
    private JobConfigurationService jobConfigurationService;

    private User testUser;
    private JobConfiguration testJobConfig;
    private String jobConfigId;
    private String userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID().toString();
        jobConfigId = UUID.randomUUID().toString();

        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("test@example.com");

        testJobConfig = new JobConfiguration();
        testJobConfig.setId(jobConfigId);
        testJobConfig.setName("Test Job Config");
        testJobConfig.setCreatedBy(testUser);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(jobConfigRepository.findByIdAndCreatedBy(jobConfigId, testUser)).thenReturn(Optional.of(testJobConfig));
    }

    @Test
    void deleteJobConfiguration_success_noExecutions() {
        // Arrange
        when(jobExecutionRepository.findByJobConfigurationAndStatusIn(eq(testJobConfig), anyList()))
                .thenReturn(Collections.emptyList());
        when(jobExecutionRepository.findByJobConfigurationOrderByStartedAtDesc(testJobConfig))
                .thenReturn(Collections.emptyList());

        // Act
        boolean result = jobConfigurationService.deleteJobConfiguration(jobConfigId, userId);

        // Assert
        assertTrue(result);
        verify(jobConfigRepository, times(1)).delete(testJobConfig);
        verify(jobExecutionRepository, never()).delete(any(JobExecution.class));
        verify(jobExecutionLogRepository, never()).deleteByExecution(any(JobExecution.class));
    }

    @Test
    void deleteJobConfiguration_success_withCompletedExecutions() {
        // Arrange
        JobExecution completedExecution1 = new JobExecution();
        completedExecution1.setId(UUID.randomUUID().toString());
        completedExecution1.setStatus(ExecutionStatus.COMPLETED);
        completedExecution1.setJobConfiguration(testJobConfig);

        JobExecution failedExecution2 = new JobExecution();
        failedExecution2.setId(UUID.randomUUID().toString());
        failedExecution2.setStatus(ExecutionStatus.FAILED);
        failedExecution2.setJobConfiguration(testJobConfig);

        List<JobExecution> completedExecutions = List.of(completedExecution1, failedExecution2);

        when(jobExecutionRepository.findByJobConfigurationAndStatusIn(eq(testJobConfig), anyList()))
                .thenReturn(Collections.emptyList()); // No active executions
        when(jobExecutionRepository.findByJobConfigurationOrderByStartedAtDesc(testJobConfig))
                .thenReturn(completedExecutions);

        // Act
        boolean result = jobConfigurationService.deleteJobConfiguration(jobConfigId, userId);

        // Assert
        assertTrue(result);
        verify(jobExecutionLogRepository, times(1)).deleteByExecution(completedExecution1);
        verify(jobExecutionRepository, times(1)).delete(completedExecution1);
        verify(jobExecutionLogRepository, times(1)).deleteByExecution(failedExecution2);
        verify(jobExecutionRepository, times(1)).delete(failedExecution2);
        verify(jobConfigRepository, times(1)).delete(testJobConfig);
    }

    @Test
    void deleteJobConfiguration_throwsException_withActiveExecutions() {
        // Arrange
        JobExecution activeExecution = new JobExecution();
        activeExecution.setId(UUID.randomUUID().toString());
        activeExecution.setStatus(ExecutionStatus.RUNNING);
        activeExecution.setJobConfiguration(testJobConfig);

        when(jobExecutionRepository.findByJobConfigurationAndStatusIn(eq(testJobConfig), anyList()))
                .thenReturn(List.of(activeExecution));

        // Act & Assert
        JobConfigurationDeletionException thrown = assertThrows(JobConfigurationDeletionException.class, () -> {
            jobConfigurationService.deleteJobConfiguration(jobConfigId, userId);
        });

        assertEquals("Cannot delete job configuration with active executions. Please wait for all executions to complete or cancel them.", thrown.getMessage());
        verify(jobConfigRepository, never()).delete(any(JobConfiguration.class));
        verify(jobExecutionRepository, never()).delete(any(JobExecution.class));
        verify(jobExecutionLogRepository, never()).deleteByExecution(any(JobExecution.class));
    }

    @Test
    void deleteJobConfiguration_notFound() {
        // Arrange
        when(jobConfigRepository.findByIdAndCreatedBy(jobConfigId, testUser)).thenReturn(Optional.empty());

        // Act
        boolean result = jobConfigurationService.deleteJobConfiguration(jobConfigId, userId);

        // Assert
        assertFalse(result);
        verify(jobConfigRepository, never()).delete(any(JobConfiguration.class));
        verify(jobExecutionRepository, never()).findByJobConfigurationAndStatusIn(any(JobConfiguration.class), anyList());
    }

    @Test
    void deleteJobConfiguration_userNotFound() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            jobConfigurationService.deleteJobConfiguration(jobConfigId, userId);
        });

        assertEquals("User not found", thrown.getMessage());
        verify(jobConfigRepository, never()).delete(any(JobConfiguration.class));
    }
}
