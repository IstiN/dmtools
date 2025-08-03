package com.github.istin.dmtools.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.istin.dmtools.auth.model.User;
import com.github.istin.dmtools.auth.repository.UserRepository;
import com.github.istin.dmtools.dto.*;
import com.github.istin.dmtools.server.model.JobConfiguration;
import com.github.istin.dmtools.server.repository.JobConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing job configurations.
 */
@Service
public class JobConfigurationService {

    private final JobConfigurationRepository jobConfigRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public JobConfigurationService(
            JobConfigurationRepository jobConfigRepository,
            UserRepository userRepository,
            ObjectMapper objectMapper) {
        this.jobConfigRepository = jobConfigRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Get all job configurations accessible to a user.
     *
     * @param userId The ID of the user
     * @return List of job configuration DTOs
     */
    @Transactional(readOnly = true)
    public List<JobConfigurationDto> getJobConfigurationsForUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        List<JobConfiguration> jobConfigs = jobConfigRepository.findAccessibleToUser(user);
        return jobConfigs.stream()
                .map(JobConfigurationDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get enabled job configurations accessible to a user.
     *
     * @param userId The ID of the user
     * @return List of enabled job configuration DTOs
     */
    @Transactional(readOnly = true)
    public List<JobConfigurationDto> getEnabledJobConfigurationsForUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        List<JobConfiguration> jobConfigs = jobConfigRepository.findEnabledAccessibleToUser(user);
        return jobConfigs.stream()
                .map(JobConfigurationDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific job configuration by ID.
     *
     * @param jobConfigId The ID of the job configuration
     * @param userId The ID of the user requesting access
     * @return The job configuration DTO
     */
    @Transactional(readOnly = true)
    public Optional<JobConfigurationDto> getJobConfiguration(String jobConfigId, String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        return jobConfigRepository.findByIdAndCreatedBy(jobConfigId, user)
                .map(JobConfigurationDto::fromEntity);
    }

    /**
     * Create a new job configuration.
     *
     * @param request The create job configuration request
     * @param userId The ID of the user creating the job configuration
     * @return The created job configuration DTO
     */
    @Transactional
    public JobConfigurationDto createJobConfiguration(CreateJobConfigurationRequest request, String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        JobConfiguration jobConfig = new JobConfiguration();
        jobConfig.setName(request.getName());
        jobConfig.setDescription(request.getDescription());
        jobConfig.setJobType(request.getJobType());
        jobConfig.setCreatedBy(user);
        jobConfig.setEnabled(request.getEnabled() != null ? request.getEnabled() : true);
        
        // Convert JsonNode to string for storage
        try {
            jobConfig.setJobParameters(objectMapper.writeValueAsString(request.getJobParameters()));
            jobConfig.setIntegrationMappings(objectMapper.writeValueAsString(request.getIntegrationMappings()));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON in request: " + e.getMessage());
        }
        
        JobConfiguration savedJobConfig = jobConfigRepository.save(jobConfig);
        return JobConfigurationDto.fromEntity(savedJobConfig);
    }

    /**
     * Update an existing job configuration.
     *
     * @param jobConfigId The ID of the job configuration to update
     * @param request The update job configuration request
     * @param userId The ID of the user updating the job configuration
     * @return The updated job configuration DTO
     */
    @Transactional
    public Optional<JobConfigurationDto> updateJobConfiguration(
            String jobConfigId, UpdateJobConfigurationRequest request, String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Optional<JobConfiguration> optionalJobConfig = jobConfigRepository.findByIdAndCreatedBy(jobConfigId, user);
        if (optionalJobConfig.isEmpty()) {
            return Optional.empty();
        }
        
        JobConfiguration jobConfig = optionalJobConfig.get();
        
        // Update only provided fields
        if (request.getName() != null) {
            jobConfig.setName(request.getName());
        }
        if (request.getDescription() != null) {
            jobConfig.setDescription(request.getDescription());
        }
        if (request.getJobType() != null) {
            jobConfig.setJobType(request.getJobType());
        }
        if (request.getEnabled() != null) {
            jobConfig.setEnabled(request.getEnabled());
        }
        
        // Update JSON fields if provided
        try {
            if (request.getJobParameters() != null) {
                jobConfig.setJobParameters(objectMapper.writeValueAsString(request.getJobParameters()));
            }
            if (request.getIntegrationMappings() != null) {
                jobConfig.setIntegrationMappings(objectMapper.writeValueAsString(request.getIntegrationMappings()));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON in request: " + e.getMessage());
        }
        
        JobConfiguration savedJobConfig = jobConfigRepository.save(jobConfig);
        return Optional.of(JobConfigurationDto.fromEntity(savedJobConfig));
    }

    /**
     * Delete a job configuration.
     *
     * @param jobConfigId The ID of the job configuration to delete
     * @param userId The ID of the user deleting the job configuration
     * @return true if deletion was successful, false if job configuration not found
     */
    @Transactional
    public boolean deleteJobConfiguration(String jobConfigId, String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Optional<JobConfiguration> optionalJobConfig = jobConfigRepository.findByIdAndCreatedBy(jobConfigId, user);
        if (optionalJobConfig.isEmpty()) {
            return false;
        }
        
        jobConfigRepository.delete(optionalJobConfig.get());
        return true;
    }

    /**
     * Get job configuration for execution with parameter and integration overrides.
     *
     * @param jobConfigId The ID of the job configuration
     * @param request The execution request with optional overrides
     * @param userId The ID of the user executing the job
     * @return The merged execution parameters
     */
    public Optional<ExecutionParametersDto> getExecutionParameters(
            String jobConfigId, ExecuteJobConfigurationRequest request, String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Optional<JobConfiguration> optionalJobConfig = jobConfigRepository.findByIdAndCreatedBy(jobConfigId, user);
        if (optionalJobConfig.isEmpty()) {
            return Optional.empty();
        }
        
        JobConfiguration jobConfig = optionalJobConfig.get();
        
        try {
            // Parse saved parameters and integrations
            JsonNode savedParameters = objectMapper.readTree(jobConfig.getJobParameters());
            JsonNode savedIntegrations = objectMapper.readTree(jobConfig.getIntegrationMappings());
            
            // Merge with overrides
            JsonNode finalParameters = mergeJsonNodes(savedParameters, request.getParameterOverrides());
            JsonNode finalIntegrations = mergeJsonNodes(savedIntegrations, request.getIntegrationOverrides());
            
            ExecutionParametersDto executionParams = new ExecutionParametersDto();
            executionParams.setJobType(jobConfig.getJobType());
            executionParams.setJobParameters(finalParameters);
            executionParams.setIntegrationMappings(finalIntegrations);
            executionParams.setExecutionMode(request.getExecutionMode() != null ? 
                    request.getExecutionMode() : "SERVER_MANAGED");
            
            return Optional.of(executionParams);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error processing job configuration: " + e.getMessage());
        }
    }

    /**
     * Record execution of a job configuration.
     *
     * @param jobConfigId The ID of the job configuration
     * @param userId The ID of the user executing the job
     */
    @Transactional
    public void recordExecution(String jobConfigId, String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Optional<JobConfiguration> optionalJobConfig = jobConfigRepository.findByIdAndCreatedBy(jobConfigId, user);
        if (optionalJobConfig.isPresent()) {
            JobConfiguration jobConfig = optionalJobConfig.get();
            jobConfig.recordExecution();
            jobConfigRepository.save(jobConfig);
        }
    }

    /**
     * Merge JSON nodes, with override values taking precedence.
     *
     * @param base The base JSON node
     * @param override The override JSON node (can be null)
     * @return The merged JSON node
     */
    private JsonNode mergeJsonNodes(JsonNode base, JsonNode override) {
        if (override == null || override.isNull()) {
            return base;
        }
        
        if (base == null || base.isNull()) {
            return override;
        }
        
        if (!base.isObject() || !override.isObject()) {
            return override; // Override takes precedence for non-objects
        }
        
        ObjectNode result = base.deepCopy();
        override.fields().forEachRemaining(entry -> {
            result.set(entry.getKey(), entry.getValue());
        });
        
        return result;
    }
} 