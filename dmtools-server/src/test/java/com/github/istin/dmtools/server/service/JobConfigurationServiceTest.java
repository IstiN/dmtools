package com.github.istin.dmtools.server.service;

import com.github.istin.dmtools.auth.model.User;
import com.github.istin.dmtools.auth.repository.UserRepository;
import com.github.istin.dmtools.server.exception.JobHasActiveExecutionsException;
import com.github.istin.dmtools.server.exception.ValidationException;
import com.github.istin.dmtools.server.model.ExecutionStatus;
import com.github.istin.dmtools.server.model.JobConfiguration;
import com.github.istin.dmtools.server.model.JobExecution;
import com.github.istin.dmtools.server.repository.JobConfigurationRepository;
import com.github.istin.dmtools.server.repository.JobExecutionLogRepository;
import com.github.istin.dmtools.server.repository.JobExecutionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class JobConfigurationServiceTest {

    @Mock
    private JobConfigurationRepository jobConfigRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private DotNotationTransformer dotNotationTransformer;
    @Mock
    private ParameterValidator parameterValidator;
    @Mock
    private JobExecutionRepository jobExecutionRepository;
    @Mock
    private JobExecutionLogRepository jobExecutionLogRepository;

    @InjectMocks
    private JobConfigurationService jobConfigurationService;

    private User testUser;
    private JobConfiguration testJobConfig;
    private String jobConfigId = "test-job-config-id";
    private String userId = "test-user-id";

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("test@example.com");

        testJobConfig = new JobConfiguration();
        testJobConfig.setId(jobConfigId);
        testJobConfig.setCreatedBy(testUser);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(jobConfigRepository.findByIdAndCreatedBy(jobConfigId, testUser)).thenReturn(Optional.of(testJobConfig));
    }

    @Test
    void deleteJobConfiguration_Success_NoExecutions() {
        when(jobExecutionRepository.findByJobConfigurationOrderByStartedAtDesc(testJobConfig))
                .thenReturn(Collections.emptyList());

        boolean deleted = jobConfigurationService.deleteJobConfiguration(jobConfigId, userId);

        assertTrue(deleted);
        verify(jobExecutionRepository, times(1)).findByJobConfigurationOrderByStartedAtDesc(testJobConfig);
        verify(jobExecutionLogRepository, never()).deleteByExecution(any(JobExecution.class));
        verify(jobExecutionRepository, never()).delete(any(JobExecution.class));
        verify(jobConfigRepository, times(1)).delete(testJobConfig);
    }

    @Test
    void deleteJobConfiguration_Success_CompletedExecutions() {
        JobExecution completedExecution1 = new JobExecution();
        completedExecution1.setStatus(ExecutionStatus.COMPLETED);
        JobExecution completedExecution2 = new JobExecution();
        completedExecution2.setStatus(ExecutionStatus.FAILED);

        List<JobExecution> completedExecutions = Arrays.asList(completedExecution1, completedExecution2);

        when(jobExecutionRepository.findByJobConfigurationOrderByStartedAtDesc(testJobConfig))
                .thenReturn(completedExecutions);

        boolean deleted = jobConfigurationService.deleteJobConfiguration(jobConfigId, userId);

        assertTrue(deleted);
        verify(jobExecutionRepository, times(1)).findByJobConfigurationOrderByStartedAtDesc(testJobConfig);
        verify(jobExecutionLogRepository, times(1)).deleteByExecution(completedExecution1);
        verify(jobExecutionLogRepository, times(1)).deleteByExecution(completedExecution2);
        verify(jobExecutionRepository, times(1)).delete(completedExecution1);
        verify(jobExecutionRepository, times(1)).delete(completedExecution2);
        verify(jobConfigRepository, times(1)).delete(testJobConfig);
    }

    @Test
    void deleteJobConfiguration_ThrowsException_ActiveExecutions() {
        JobExecution activeExecution = new JobExecution();
        activeExecution.setStatus(ExecutionStatus.RUNNING); // Active status
        JobExecution completedExecution = new JobExecution();
        completedExecution.setStatus(ExecutionStatus.COMPLETED);

        List<JobExecution> mixedExecutions = Arrays.asList(activeExecution, completedExecution);

        when(jobExecutionRepository.findByJobConfigurationOrderByStartedAtDesc(testJobConfig))
                .thenReturn(mixedExecutions);

        JobHasActiveExecutionsException thrown = assertThrows(JobHasActiveExecutionsException.class, () -> {
            jobConfigurationService.deleteJobConfiguration(jobConfigId, userId);
        });

        assertTrue(thrown.getMessage().contains("Cannot delete job configuration with active executions"));
        verify(jobExecutionRepository, times(1)).findByJobConfigurationOrderByStartedAtDesc(testJobConfig);
        verify(jobExecutionLogRepository, never()).deleteByExecution(any(JobExecution.class));
        verify(jobExecutionRepository, never()).delete(any(JobExecution.class));
        verify(jobConfigRepository, never()).delete(any(JobConfiguration.class));
    }

    @Test
    void deleteJobConfiguration_NotFound() {
        when(jobConfigRepository.findByIdAndCreatedBy(jobConfigId, testUser)).thenReturn(Optional.empty());

        boolean deleted = jobConfigurationService.deleteJobConfiguration(jobConfigId, userId);

        assertFalse(deleted);
        verify(jobExecutionRepository, never()).findByJobConfigurationOrderByStartedAtDesc(any(JobConfiguration.class));
        verify(jobExecutionLogRepository, never()).deleteByExecution(any(JobExecution.class));
        verify(jobExecutionRepository, never()).delete(any(JobExecution.class));
        verify(jobConfigRepository, never()).delete(any(JobConfiguration.class));
    }

    @Test
    void deleteJobConfiguration_UserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            jobConfigurationService.deleteJobConfiguration(jobConfigId, userId);
        });

        assertTrue(thrown.getMessage().contains("User not found"));
        verify(jobConfigRepository, never()).findByIdAndCreatedBy(anyString(), any(User.class));
    }
}
