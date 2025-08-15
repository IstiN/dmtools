package com.github.istin.dmtools.server.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.istin.dmtools.auth.model.User;
import com.github.istin.dmtools.auth.repository.UserRepository;
import com.github.istin.dmtools.dto.CreateJobConfigurationRequest;
import com.github.istin.dmtools.dto.JobConfigurationDto;
import com.github.istin.dmtools.server.exception.JobConfigurationDeletionException;
import com.github.istin.dmtools.server.exception.ValidationException;
import com.github.istin.dmtools.server.model.JobConfiguration;
import com.github.istin.dmtools.server.model.JobExecution;
import com.github.istin.dmtools.server.repository.JobConfigurationRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobConfigurationServiceTest {

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

    @InjectMocks
    private JobConfigurationService jobConfigurationService;

    private User testUser;
    private JobConfiguration testJobConfig;

    @BeforeEach
    void setUp() throws com.fasterxml.jackson.core.JsonProcessingException {
        testUser = new User();
        testUser.setId("user123");
        testUser.setEmail("test@example.com");

        testJobConfig = new JobConfiguration();
        testJobConfig.setId("jobConfig123");
        testJobConfig.setName("Test Job Config");
        testJobConfig.setCreatedBy(testUser);
    }

    @Test
    void testCreateJobConfiguration_Success() throws Exception {
        CreateJobConfigurationRequest request = new CreateJobConfigurationRequest();
        request.setName("New Job");
        request.setJobType("Teammate");
        request.setJobParameters(mock(com.fasterxml.jackson.databind.JsonNode.class));
        request.setIntegrationMappings(mock(com.fasterxml.jackson.databind.JsonNode.class));

        when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
        when(dotNotationTransformer.transformToNested(anyMap())).thenReturn(Collections.emptyMap());
        when(objectMapper.writeValueAsString(any(com.fasterxml.jackson.databind.JsonNode.class))).thenReturn("{}");
        when(jobConfigRepository.save(any(JobConfiguration.class))).thenReturn(testJobConfig);

        JobConfigurationDto result = jobConfigurationService.createJobConfiguration(request, "user123");

        assertNotNull(result);
        assertEquals("Test Job Config", result.getName());
        verify(jobConfigRepository).save(any(JobConfiguration.class));
    }

    @Test
    void testDeleteJobConfiguration_Success() {
        when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
        when(jobConfigRepository.findByIdAndCreatedBy("jobConfig123", testUser)).thenReturn(Optional.of(testJobConfig));
        when(jobExecutionRepository.findByJobConfigurationOrderByStartedAtDesc(testJobConfig)).thenReturn(Collections.emptyList());

        boolean deleted = jobConfigurationService.deleteJobConfiguration("jobConfig123", "user123");

        assertTrue(deleted);
        verify(jobConfigRepository).delete(testJobConfig);
    }

    @Test
    void testDeleteJobConfiguration_NotFound() {
        when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
        when(jobConfigRepository.findByIdAndCreatedBy("jobConfig123", testUser)).thenReturn(Optional.empty());

        boolean deleted = jobConfigurationService.deleteJobConfiguration("jobConfig123", "user123");

        assertFalse(deleted);
        verify(jobConfigRepository, never()).delete(any(JobConfiguration.class));
    }

    @Test
    void testDeleteJobConfiguration_WithExistingExecutions() {
        JobExecution existingExecution = new JobExecution();
        existingExecution.setId("exec1");
        existingExecution.setJobConfiguration(testJobConfig);

        when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
        when(jobConfigRepository.findByIdAndCreatedBy("jobConfig123", testUser)).thenReturn(Optional.of(testJobConfig));
        when(jobExecutionRepository.findByJobConfigurationOrderByStartedAtDesc(testJobConfig)).thenReturn(List.of(existingExecution));

        JobConfigurationDeletionException thrown = assertThrows(JobConfigurationDeletionException.class,
                () -> jobConfigurationService.deleteJobConfiguration("jobConfig123", "user123"));

        assertEquals("Cannot delete job configuration with existing job executions.", thrown.getMessage());
        verify(jobConfigRepository, never()).delete(any(JobConfiguration.class));
    }

    // Add more tests for other methods in JobConfigurationService as needed

}