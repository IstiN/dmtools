package com.github.istin.dmtools.auth.service;

import com.github.istin.dmtools.auth.model.*;
import com.github.istin.dmtools.auth.repository.*;
import com.github.istin.dmtools.auth.util.EncryptionUtils;
import com.github.istin.dmtools.dto.*;
import com.github.istin.dmtools.github.GitHub;
import com.github.istin.dmtools.github.BasicGithub;
import com.github.istin.dmtools.common.code.model.SourceCodeConfig;
import com.github.istin.dmtools.atlassian.confluence.Confluence;
import com.github.istin.dmtools.atlassian.jira.JiraClient;
import com.github.istin.dmtools.atlassian.jira.model.Ticket;
import com.github.istin.dmtools.common.model.IUser;
import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;

/**
 * Service for managing integrations.
 */
@Service
public class IntegrationService {

    private final IntegrationRepository integrationRepository;
    private final IntegrationConfigRepository configRepository;
    private final IntegrationUserRepository userRepository;
    private final IntegrationWorkspaceRepository workspaceRepository;
    private final UserRepository userRepo;
    private final WorkspaceRepository workspaceRepo;
    private final EncryptionUtils encryptionUtils;
    private final IntegrationConfigurationLoader configurationLoader;

    @Autowired
    public IntegrationService(
            IntegrationRepository integrationRepository,
            IntegrationConfigRepository configRepository,
            IntegrationUserRepository userRepository,
            IntegrationWorkspaceRepository workspaceRepository,
            UserRepository userRepo,
            WorkspaceRepository workspaceRepo,
            EncryptionUtils encryptionUtils,
            IntegrationConfigurationLoader configurationLoader) {
        this.integrationRepository = integrationRepository;
        this.configRepository = configRepository;
        this.userRepository = userRepository;
        this.workspaceRepository = workspaceRepository;
        this.userRepo = userRepo;
        this.workspaceRepo = workspaceRepo;
        this.encryptionUtils = encryptionUtils;
        this.configurationLoader = configurationLoader;
    }

    /**
     * Get all integrations accessible to a user.
     *
     * @param userId The ID of the user
     * @return List of integration DTOs
     */
    @Transactional(readOnly = true)
    public List<IntegrationDto> getIntegrationsForUser(String userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        List<Integration> integrations = integrationRepository.findAccessibleToUser(user);
        return integrations.stream()
                .map(integration -> {
                    // Get categories for this integration type
                    List<String> categories = getCategoriesForIntegrationType(integration.getType());
                    return IntegrationDto.fromEntityWithCategories(integration, categories);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get categories for a specific integration type.
     * 
     * @param integrationType The integration type (e.g., "jira", "dial")
     * @return List of categories for this integration type
     */
    private List<String> getCategoriesForIntegrationType(String integrationType) {
        try {
            if (configurationLoader.hasIntegrationType(integrationType)) {
                IntegrationTypeDto integrationTypeDto = configurationLoader.getIntegrationType(integrationType);
                return integrationTypeDto.getCategories();
            }
        } catch (Exception e) {
            // Log but don't fail - just return empty categories
            System.out.println("Warning: Could not load categories for integration type '" + integrationType + "': " + e.getMessage());
        }
        return new ArrayList<>();
    }

    /**
     * Get integrations for a specific workspace.
     *
     * @param workspaceId The ID of the workspace
     * @return List of integration DTOs
     */
    @Transactional(readOnly = true)
    public List<IntegrationDto> getIntegrationsForWorkspace(String workspaceId) {
        Workspace workspace = workspaceRepo.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found"));
        
        List<Integration> integrations = integrationRepository.findByWorkspace(workspace);
        return integrations.stream()
                .map(integration -> {
                    // Get categories for this integration type
                    List<String> categories = getCategoriesForIntegrationType(integration.getType());
                    return IntegrationDto.fromEntityWithCategories(integration, categories);
                })
                .collect(Collectors.toList());
    }

    /**
     * Get an integration by ID.
     *
     * @param integrationId The ID of the integration
     * @param userId The ID of the user requesting the integration
     * @param includeSensitive Whether to include sensitive configuration values
     * @return The integration DTO
     */
    @Transactional(readOnly = true)
    public IntegrationDto getIntegrationById(String integrationId, String userId, boolean includeSensitive) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Integration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new IllegalArgumentException("Integration not found"));
        
        // Manually load config params since the entity relationship is broken
        var manualConfigs = configRepository.findAll().stream()
                .filter(c -> c.getIntegration().getId().equals(integration.getId()))
                .toList();
        
        // Config params loaded successfully via manual approach
        
        // Check if the user has access to this integration
        boolean isCreator = integration.getCreatedBy().getId().equals(userId);
        boolean isSharedWithUser = integration.getUsers().stream()
                .anyMatch(iu -> iu.getUser().getId().equals(userId));
        
        if (!isCreator && !isSharedWithUser) {
            throw new IllegalArgumentException("User does not have access to this integration");
        }
        
        // Only include sensitive data if requested and user is the creator or has ADMIN permission
        boolean canViewSensitiveData = isCreator || integration.getUsers().stream()
                .anyMatch(iu -> iu.getUser().getId().equals(userId) && 
                         iu.getPermissionLevel() == IntegrationPermissionLevel.ADMIN);
        
        if (includeSensitive && canViewSensitiveData) {
            return createDtoWithSensitiveData(integration, manualConfigs);
        } else {
            // Get categories for this integration type
            List<String> categories = getCategoriesForIntegrationType(integration.getType());
            return IntegrationDto.fromEntityWithManualConfigAndCategories(integration, manualConfigs, categories);
        }
    }

    /**
     * Helper method to create DTO with sensitive data and manual config params.
     */
    private IntegrationDto createDtoWithSensitiveData(Integration integration, java.util.List<IntegrationConfig> manualConfigs) {
        IntegrationDto dto = new IntegrationDto();
        dto.setId(integration.getId());
        dto.setName(integration.getName());
        dto.setDescription(integration.getDescription());
        dto.setType(integration.getType());
        dto.setEnabled(integration.isEnabled());
        dto.setCreatedById(integration.getCreatedBy().getId());
        dto.setCreatedByName(integration.getCreatedBy().getName());
        dto.setCreatedByEmail(integration.getCreatedBy().getEmail());
        dto.setUsageCount(integration.getUsageCount());
        dto.setCreatedAt(integration.getCreatedAt());
        dto.setUpdatedAt(integration.getUpdatedAt());
        dto.setLastUsedAt(integration.getLastUsedAt());
        
        // Get categories for this integration type
        List<String> categories = getCategoriesForIntegrationType(integration.getType());
        dto.setCategories(new ArrayList<>(categories));
        
        // Convert manually provided config params, including sensitive values
        dto.setConfigParams(manualConfigs.stream()
                .map(config -> {
                    IntegrationConfigDto configDto = new IntegrationConfigDto();
                    configDto.setId(config.getId());
                    configDto.setParamKey(config.getParamKey());
                    // Include all values, decrypting sensitive ones
                    if (config.isSensitive() && config.getParamValue() != null) {
                        configDto.setParamValue(encryptionUtils.decrypt(config.getParamValue()));
                    } else {
                        configDto.setParamValue(config.getParamValue());
                    }
                    configDto.setSensitive(config.isSensitive());
                    return configDto;
                })
                .collect(java.util.stream.Collectors.toSet()));
        
        return dto;
    }

    /**
     * Create a new integration.
     *
     * @param request The create integration request
     * @param userId The ID of the user creating the integration
     * @return The created integration DTO
     */
    @Transactional
    public IntegrationDto createIntegration(CreateIntegrationRequest request, String userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Validate parameter keys against JSON configuration
        if (request.getConfigParams() != null && !request.getConfigParams().isEmpty()) {
            configurationLoader.validateParameterKeys(request.getType(), request.getConfigParams().keySet());
        }
        
        Integration integration = new Integration();
        integration.setName(request.getName());
        integration.setDescription(request.getDescription());
        integration.setType(request.getType());
        integration.setEnabled(true);
        integration.setCreatedBy(user);
        integration.setUsageCount(0);
        
        Integration savedIntegration = integrationRepository.save(integration);
        
        // Save configuration parameters
        if (request.getConfigParams() != null) {
            for (Map.Entry<String, CreateIntegrationRequest.ConfigParam> entry : request.getConfigParams().entrySet()) {
                IntegrationConfig config = new IntegrationConfig();
                config.setIntegration(savedIntegration);
                config.setParamKey(entry.getKey());
                
                CreateIntegrationRequest.ConfigParam param = entry.getValue();
                config.setSensitive(param.isSensitive());
                
                // Encrypt sensitive values
                if (param.isSensitive() && param.getValue() != null) {
                    config.setParamValue(encryptionUtils.encrypt(param.getValue()));
                } else {
                    config.setParamValue(param.getValue());
                }
                
                configRepository.save(config);
            }
        }
        
        // Force flush to ensure all data is written to database before reload
        configRepository.flush();
        
        // Use regular findById 
        Integration reloadedIntegration = integrationRepository.findById(savedIntegration.getId())
                .orElseThrow(() -> new IllegalStateException("Integration not found after save"));
        
        // Manually load config params since the entity relationship is broken
        var manualConfigs = configRepository.findAll().stream()
                .filter(c -> c.getIntegration().getId().equals(reloadedIntegration.getId()))
                .toList();
        
        // Create DTO with manual config params and categories
        List<String> categories = getCategoriesForIntegrationType(reloadedIntegration.getType());
        return IntegrationDto.fromEntityWithManualConfigAndCategories(reloadedIntegration, manualConfigs, categories);
    }

    /**
     * Update an existing integration.
     *
     * @param integrationId The ID of the integration to update
     * @param request The update integration request
     * @param userId The ID of the user updating the integration
     * @return The updated integration DTO
     */
    @Transactional
    public IntegrationDto updateIntegration(String integrationId, UpdateIntegrationRequest request, String userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Integration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new IllegalArgumentException("Integration not found"));
        
        // Check if the user has permission to update this integration
        boolean isCreator = integration.getCreatedBy().getId().equals(userId);
        boolean hasWritePermission = integration.getUsers().stream()
                .anyMatch(iu -> iu.getUser().getId().equals(userId) && 
                         (iu.getPermissionLevel() == IntegrationPermissionLevel.WRITE || 
                          iu.getPermissionLevel() == IntegrationPermissionLevel.ADMIN));
        
        if (!isCreator && !hasWritePermission) {
            throw new IllegalArgumentException("User does not have permission to update this integration");
        }
        
        // Update basic properties if provided
        if (request.getName() != null) {
            integration.setName(request.getName());
        }
        
        if (request.getDescription() != null) {
            integration.setDescription(request.getDescription());
        }
        
        if (request.getEnabled() != null) {
            integration.setEnabled(request.getEnabled());
        }
        
        // Update configuration parameters if provided
        if (request.getConfigParams() != null) {
            // Validate parameter keys against JSON configuration
            if (!request.getConfigParams().isEmpty()) {
                configurationLoader.validateParameterKeys(integration.getType(), request.getConfigParams().keySet());
            }
            
            for (Map.Entry<String, UpdateIntegrationRequest.ConfigParam> entry : request.getConfigParams().entrySet()) {
                String paramKey = entry.getKey();
                UpdateIntegrationRequest.ConfigParam param = entry.getValue();
                
                // Find existing config or create new one
                IntegrationConfig config = configRepository.findByIntegrationAndParamKey(integration, paramKey)
                        .orElseGet(() -> {
                            IntegrationConfig newConfig = new IntegrationConfig();
                            newConfig.setIntegration(integration);
                            newConfig.setParamKey(paramKey);
                            return newConfig;
                        });
                
                config.setSensitive(param.isSensitive());
                
                // Encrypt sensitive values
                if (param.isSensitive() && param.getValue() != null) {
                    config.setParamValue(encryptionUtils.encrypt(param.getValue()));
                } else {
                    config.setParamValue(param.getValue());
                }
                
                configRepository.save(config);
            }
        }
        
        Integration updatedIntegration = integrationRepository.save(integration);
        // Get categories for this integration type
        List<String> categories = getCategoriesForIntegrationType(updatedIntegration.getType());
        return IntegrationDto.fromEntityWithCategories(updatedIntegration, categories);
    }

    /**
     * Delete an integration.
     *
     * @param integrationId The ID of the integration to delete
     * @param userId The ID of the user deleting the integration
     */
    @Transactional
    public void deleteIntegration(String integrationId, String userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Integration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new IllegalArgumentException("Integration not found"));
        
        // Check if the user has permission to delete this integration
        boolean isCreator = integration.getCreatedBy().getId().equals(userId);
        boolean hasAdminPermission = integration.getUsers().stream()
                .anyMatch(iu -> iu.getUser().getId().equals(userId) && 
                         iu.getPermissionLevel() == IntegrationPermissionLevel.ADMIN);
        
        if (!isCreator && !hasAdminPermission) {
            throw new IllegalArgumentException("User does not have permission to delete this integration");
        }
        
        // Delete all related configuration parameters first to avoid foreign key constraint violations
        configRepository.deleteByIntegration(integration);
        
        // Delete the integration and all remaining related entities
        integrationRepository.delete(integration);
    }

    /**
     * Enable or disable an integration.
     *
     * @param integrationId The ID of the integration
     * @param userId The ID of the user
     * @param enabled Whether to enable or disable the integration
     * @return The updated integration DTO
     */
    @Transactional
    public IntegrationDto setIntegrationEnabled(String integrationId, String userId, boolean enabled) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Integration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new IllegalArgumentException("Integration not found"));
        
        // Check if the user has permission to update this integration
        boolean isCreator = integration.getCreatedBy().getId().equals(userId);
        boolean hasWritePermission = integration.getUsers().stream()
                .anyMatch(iu -> iu.getUser().getId().equals(userId) && 
                         (iu.getPermissionLevel() == IntegrationPermissionLevel.WRITE || 
                          iu.getPermissionLevel() == IntegrationPermissionLevel.ADMIN));
        
        if (!isCreator && !hasWritePermission) {
            throw new IllegalArgumentException("User does not have permission to update this integration");
        }
        
        integration.setEnabled(enabled);
        Integration updatedIntegration = integrationRepository.save(integration);
        // Get categories for this integration type
        List<String> categories = getCategoriesForIntegrationType(updatedIntegration.getType());
        return IntegrationDto.fromEntityWithCategories(updatedIntegration, categories);
    }

    /**
     * Share an integration with a user.
     *
     * @param integrationId The ID of the integration
     * @param request The share integration request
     * @param userId The ID of the user sharing the integration
     * @return The integration user DTO
     */
    @Transactional
    public IntegrationUserDto shareIntegrationWithUser(String integrationId, ShareIntegrationRequest request, String userId) {
        User currentUser = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Integration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new IllegalArgumentException("Integration not found"));
        
        // Check if the current user has permission to share this integration
        boolean isCreator = integration.getCreatedBy().getId().equals(userId);
        boolean hasAdminPermission = integration.getUsers().stream()
                .anyMatch(iu -> iu.getUser().getId().equals(userId) && 
                         iu.getPermissionLevel() == IntegrationPermissionLevel.ADMIN);
        
        if (!isCreator && !hasAdminPermission) {
            throw new IllegalArgumentException("User does not have permission to share this integration");
        }
        
        // Find the target user by email
        User targetUser = userRepo.findByEmail(request.getUserEmail())
                .orElseThrow(() -> new IllegalArgumentException("Target user not found"));
        
        // Check if the integration is already shared with this user
        Optional<IntegrationUser> existingShare = userRepository.findByIntegrationAndUser(integration, targetUser);
        if (existingShare.isPresent()) {
            // Update the permission level
            IntegrationUser integrationUser = existingShare.get();
            integrationUser.setPermissionLevel(request.getPermissionLevel());
            IntegrationUser updatedIntegrationUser = userRepository.save(integrationUser);
            return IntegrationUserDto.fromEntity(updatedIntegrationUser);
        } else {
            // Create a new share
            IntegrationUser integrationUser = new IntegrationUser();
            integrationUser.setIntegration(integration);
            integrationUser.setUser(targetUser);
            integrationUser.setPermissionLevel(request.getPermissionLevel());
            IntegrationUser savedIntegrationUser = userRepository.save(integrationUser);
            return IntegrationUserDto.fromEntity(savedIntegrationUser);
        }
    }

    /**
     * Remove a user's access to an integration.
     *
     * @param integrationId The ID of the integration
     * @param targetUserId The ID of the user to remove
     * @param userId The ID of the user removing access
     */
    @Transactional
    public void removeUserAccess(String integrationId, String targetUserId, String userId) {
        User currentUser = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Integration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new IllegalArgumentException("Integration not found"));
        
        // Check if the current user has permission to manage access to this integration
        boolean isCreator = integration.getCreatedBy().getId().equals(userId);
        boolean hasAdminPermission = integration.getUsers().stream()
                .anyMatch(iu -> iu.getUser().getId().equals(userId) && 
                         iu.getPermissionLevel() == IntegrationPermissionLevel.ADMIN);
        
        if (!isCreator && !hasAdminPermission) {
            throw new IllegalArgumentException("User does not have permission to manage access to this integration");
        }
        
        // Find the target user
        User targetUser = userRepo.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("Target user not found"));
        
        // Remove the user's access
        userRepository.deleteByIntegrationAndUser(integration, targetUser);
    }

    /**
     * Share an integration with a workspace.
     *
     * @param integrationId The ID of the integration
     * @param request The share integration with workspace request
     * @param userId The ID of the user sharing the integration
     * @return The workspace DTO
     */
    @Transactional
    public WorkspaceDto shareIntegrationWithWorkspace(String integrationId, ShareIntegrationWithWorkspaceRequest request, String userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Integration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new IllegalArgumentException("Integration not found"));
        
        // Check if the user has permission to share this integration
        boolean isCreator = integration.getCreatedBy().getId().equals(userId);
        boolean hasAdminPermission = integration.getUsers().stream()
                .anyMatch(iu -> iu.getUser().getId().equals(userId) && 
                         iu.getPermissionLevel() == IntegrationPermissionLevel.ADMIN);
        
        if (!isCreator && !hasAdminPermission) {
            throw new IllegalArgumentException("User does not have permission to share this integration");
        }
        
        // Find the target workspace
        Workspace workspace = workspaceRepo.findById(request.getWorkspaceId())
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found"));
        
        // Check if the user has access to the workspace
        boolean isWorkspaceOwner = workspace.getOwner().getId().equals(userId);
        boolean isWorkspaceAdmin = workspace.getUsers().stream()
                .anyMatch(wu -> wu.getUser().getId().equals(userId) && wu.getRole() == WorkspaceRole.ADMIN);
        
        if (!isWorkspaceOwner && !isWorkspaceAdmin) {
            throw new IllegalArgumentException("User does not have permission to add integrations to this workspace");
        }
        
        // Check if the integration is already shared with this workspace
        Optional<IntegrationWorkspace> existingShare = workspaceRepository.findByIntegrationAndWorkspace(integration, workspace);
        if (existingShare.isPresent()) {
            // Already shared, nothing to do
            return new WorkspaceDto(
                workspace.getId(),
                workspace.getName(),
                workspace.getOwner().getId(),
                null
            );
        } else {
            // Create a new share
            IntegrationWorkspace integrationWorkspace = new IntegrationWorkspace();
            integrationWorkspace.setIntegration(integration);
            integrationWorkspace.setWorkspace(workspace);
            workspaceRepository.save(integrationWorkspace);
            
            return new WorkspaceDto(
                workspace.getId(),
                workspace.getName(),
                workspace.getOwner().getId(),
                null
            );
        }
    }

    /**
     * Remove an integration from a workspace.
     *
     * @param integrationId The ID of the integration
     * @param workspaceId The ID of the workspace
     * @param userId The ID of the user removing the integration
     */
    @Transactional
    public void removeIntegrationFromWorkspace(String integrationId, String workspaceId, String userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Integration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new IllegalArgumentException("Integration not found"));
        
        Workspace workspace = workspaceRepo.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found"));
        
        // Check if the user has permission to remove this integration from the workspace
        boolean isIntegrationCreator = integration.getCreatedBy().getId().equals(userId);
        boolean hasIntegrationAdminPermission = integration.getUsers().stream()
                .anyMatch(iu -> iu.getUser().getId().equals(userId) && 
                         iu.getPermissionLevel() == IntegrationPermissionLevel.ADMIN);
        
        boolean isWorkspaceOwner = workspace.getOwner().getId().equals(userId);
        boolean isWorkspaceAdmin = workspace.getUsers().stream()
                .anyMatch(wu -> wu.getUser().getId().equals(userId) && wu.getRole() == WorkspaceRole.ADMIN);
        
        if ((!isIntegrationCreator && !hasIntegrationAdminPermission) && 
            (!isWorkspaceOwner && !isWorkspaceAdmin)) {
            throw new IllegalArgumentException("User does not have permission to remove this integration from the workspace");
        }
        
        // Remove the integration from the workspace
        workspaceRepository.deleteByIntegrationAndWorkspace(integration, workspace);
    }

    /**
     * Record usage of an integration.
     *
     * @param integrationId The ID of the integration
     */
    @Transactional
    public void recordIntegrationUsage(String integrationId) {
        Integration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new IllegalArgumentException("Integration not found"));
        
        integration.recordUsage();
        integrationRepository.save(integration);
    }

    /**
     * Test a specific integration based on type.
     *
     * @param request The test integration request
     * @return Test result
     */
    public Map<String, Object> testIntegration(TestIntegrationRequest request) {
        return switch (request.getType()) {
            case "github" -> testGitHubIntegration(request.getConfigParams());
            case "jira" -> testJiraIntegration(request.getConfigParams());
            case "confluence" -> testConfluenceIntegration(request.getConfigParams());
            case "figma" -> testFigmaIntegration(request.getConfigParams());
            // TODO: Add cases for other integration types
            default -> {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "Unsupported integration type: " + request.getType());
                yield result;
            }
        };
    }

    /**
     * Tests a GitHub integration with the provided configuration parameters.
     *
     * @param configParams A map containing the configuration parameters for the test.
     *                     Expected keys are: SOURCE_GITHUB_TOKEN, SOURCE_GITHUB_WORKSPACE,
     *                     SOURCE_GITHUB_REPOSITORY, SOURCE_GITHUB_BRANCH, and an optional
     *                     SOURCE_GITHUB_BASE_PATH.
     * @return A map containing the result of the connection test. The map includes
     *         a "success" key with a boolean value, a "message" key with a
     *         descriptive string, and if successful, additional keys for "user",
     *         "userId", and "configuration" details.
     */
    private Map<String, Object> testGitHubIntegration(Map<String, String> configParams) {
        String token = configParams.get("SOURCE_GITHUB_TOKEN");
        String workspace = configParams.get("SOURCE_GITHUB_WORKSPACE");
        String repository = configParams.get("SOURCE_GITHUB_REPOSITORY");
        String branch = configParams.get("SOURCE_GITHUB_BRANCH");
        String basePath = configParams.getOrDefault("SOURCE_GITHUB_BASE_PATH", "https://api.github.com");

        // Create a SourceCodeConfig for testing
        SourceCodeConfig config = SourceCodeConfig.builder()
                .type(SourceCodeConfig.Type.GITHUB)
                .auth(token)
                .path(basePath)
                .workspaceName(workspace)
                .repoName(repository)
                .branchName(branch)
                .build();
        
        try {
            // Create GitHub instance
            GitHub gitHub = new BasicGithub(config);

            // Disable caching for the test
            gitHub.setClearCache(true);
            gitHub.setCacheGetRequestsEnabled(false);
            gitHub.setCachePostRequestsEnabled(false);

            // Test the connection
            Map<String, Object> result = gitHub.testConnectionDetailed();
            
            // Add configuration details to the result for context
            Map<String, String> configContext = new HashMap<>();
            configContext.put("workspace", workspace);
            configContext.put("repository", repository);
            configContext.put("branch", branch);
            configContext.put("basePath", basePath);
            result.put("configuration", configContext);
            
            return result;
        } catch (IOException e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "Failed to initialize GitHub client: " + e.getMessage());
            return errorResult;
        }
    }

    /**
     * Tests a Jira integration with the provided configuration parameters.
     *
     * @param configParams A map containing the configuration parameters for the test.
     *                     Expected keys are: JIRA_BASE_PATH, and either (JIRA_EMAIL + JIRA_API_TOKEN) or JIRA_LOGIN_PASS_TOKEN, 
     *                     and an optional JIRA_AUTH_TYPE.
     * @return A map containing the result of the connection test. The map includes
     *         a "success" key with a boolean value, a "message" key with a
     *         descriptive string, and if successful, additional keys for "user",
     *         "userId", and "configuration" details.
     */
    private Map<String, Object> testJiraIntegration(Map<String, String> configParams) {
        try {
            String basePath = configParams.get("JIRA_BASE_PATH");
            String authType = configParams.getOrDefault("JIRA_AUTH_TYPE", "Basic");
            
            // Validate required parameters
            if (basePath == null || basePath.trim().isEmpty()) {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("message", "Jira base path is required");
                errorResult.put("error", "missing_base_path");
                return errorResult;
            }
            
            // Determine authentication token using priority logic
            String token = null;
            
            // Priority 1: Use separate email and API token if both are available
            String email = configParams.get("JIRA_EMAIL");
            String apiToken = configParams.get("JIRA_API_TOKEN");
            
            if (email != null && !email.trim().isEmpty() && 
                apiToken != null && !apiToken.trim().isEmpty()) {
                // Automatically combine email:token and base64 encode
                String credentials = email.trim() + ":" + apiToken.trim();
                token = java.util.Base64.getEncoder().encodeToString(credentials.getBytes());
            } else {
                // Priority 2: Use legacy base64-encoded token
                token = configParams.get("JIRA_LOGIN_PASS_TOKEN");
            }
            
            if (token == null || token.trim().isEmpty()) {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("message", "Jira authentication is required - provide either (email + API token) or legacy base64 token");
                errorResult.put("error", "missing_authentication");
                return errorResult;
            }
            
            // Validate base path format
            if (!basePath.startsWith("http://") && !basePath.startsWith("https://")) {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("message", "Jira base path must start with http:// or https://");
                errorResult.put("error", "invalid_base_path_format");
                return errorResult;
            }
            
            // Create Jira client and test connection
            JiraClient<Ticket> jiraClient = new JiraClient<Ticket>(basePath, token) {
                @Override
                public String[] getDefaultQueryFields() {
                    return new String[]{"summary", "status"};
                }
                
                @Override
                public String[] getExtendedQueryFields() {
                    return getDefaultQueryFields();
                }
                
                @Override
                public String getTextFieldsOnly(ITicket ticket) {
                    return "";
                }
                
                @Override
                public void deleteCommentIfExists(String ticketKey, String comment) throws IOException {
                    // Not needed for testing
                }
                
                @Override
                public List<? extends ITicket> getTestCases(ITicket ticket) throws IOException {
                    return Collections.emptyList();
                }
                
                @Override
                public TrackerClient.TextType getTextType() {
                    return TrackerClient.TextType.MARKDOWN;
                }
            };
            
            if (authType != null && !authType.trim().isEmpty()) {
                jiraClient.setAuthType(authType);
            }
            jiraClient.setLogEnabled(false); // Disable logging during testing
            
            // Disable caching for the test
            jiraClient.setClearCache(true);
            jiraClient.setCacheGetRequestsEnabled(false);
            
            // Test connection by getting current user profile
            IUser user = jiraClient.performMyProfile();
            
            // If we get here, the connection test was successful
            Map<String, Object> successResult = new HashMap<>();
            successResult.put("success", true);
            successResult.put("message", "Jira connection test successful");
            
            // Add user information
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("displayName", user.getFullName());
            userInfo.put("accountId", user.getID());
            if (user.getEmailAddress() != null) {
                userInfo.put("emailAddress", user.getEmailAddress());
            }
            successResult.put("user", userInfo);
            
            // Add configuration info
            Map<String, Object> configInfo = new HashMap<>();
            configInfo.put("basePath", basePath);
            configInfo.put("authType", authType);
            successResult.put("configuration", configInfo);
            
            return successResult;
            
        } catch (IOException e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            
            String errorMessage = e.getMessage();
            if (errorMessage != null) {
                // Check for common error patterns
                if (errorMessage.contains("401") || errorMessage.contains("Unauthorized")) {
                    errorResult.put("message", "Jira authentication failed - invalid token or credentials");
                    errorResult.put("error", "authentication_failed");
                } else if (errorMessage.contains("403") || errorMessage.contains("Forbidden")) {
                    errorResult.put("message", "Jira access forbidden - insufficient permissions");
                    errorResult.put("error", "access_forbidden");
                } else if (errorMessage.contains("404") || errorMessage.contains("Not Found")) {
                    errorResult.put("message", "Jira instance not found - check base path");
                    errorResult.put("error", "instance_not_found");
                } else if (errorMessage.contains("timeout") || errorMessage.contains("ConnectException")) {
                    errorResult.put("message", "Connection timeout - Jira instance may be unreachable");
                    errorResult.put("error", "connection_timeout");
                } else {
                    errorResult.put("message", "Jira connection failed: " + errorMessage);
                    errorResult.put("error", "connection_failed");
                }
            } else {
                errorResult.put("message", "Jira connection failed with unknown error");
                errorResult.put("error", "unknown_error");
            }
            
            return errorResult;
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "Unexpected error during Jira connection test: " + e.getMessage());
            errorResult.put("error", "unexpected_error");
            return errorResult;
        }
    }

    /**
     * Tests a Confluence integration with the provided configuration parameters.
     *
     * @param configParams A map containing the configuration parameters for the test.
     *                     Expected keys are: CONFLUENCE_BASE_PATH, and either (CONFLUENCE_EMAIL + CONFLUENCE_API_TOKEN + CONFLUENCE_AUTH_TYPE) or CONFLUENCE_LOGIN_PASS_TOKEN.
     * @return A map containing the result of the connection test.
     */
    private Map<String, Object> testConfluenceIntegration(Map<String, String> configParams) {
        try {
            String basePath = configParams.get("CONFLUENCE_BASE_PATH");
            String authType = configParams.getOrDefault("CONFLUENCE_AUTH_TYPE", "Basic");
            
            // Validate required parameters
            if (basePath == null || basePath.trim().isEmpty()) {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("message", "Confluence base path is required");
                errorResult.put("error", "missing_base_path");
                return errorResult;
            }
            
            // Determine authentication token using priority logic
            String token = null;
            
            // Priority 1: Use separate email and API token if both are available
            String email = configParams.get("CONFLUENCE_EMAIL");
            String apiToken = configParams.get("CONFLUENCE_API_TOKEN");
            
            if (email != null && !email.trim().isEmpty() && 
                apiToken != null && !apiToken.trim().isEmpty()) {
                
                // For Bearer auth, use token directly without email combination
                if ("Bearer".equalsIgnoreCase(authType)) {
                    token = apiToken.trim();
                } else {
                    // For Basic auth (default), combine email:token and base64 encode
                    String credentials = email.trim() + ":" + apiToken.trim();
                    token = java.util.Base64.getEncoder().encodeToString(credentials.getBytes());
                }
            } else {
                // Priority 2: Use legacy base64-encoded token
                token = configParams.get("CONFLUENCE_LOGIN_PASS_TOKEN");
                if (token == null) {
                    // Also check for the generic CONFLUENCE_TOKEN parameter
                    token = configParams.get("CONFLUENCE_TOKEN");
                }
            }
            
            if (token == null || token.trim().isEmpty()) {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("message", "Confluence authentication is required - provide either (email + API token + auth type) or legacy base64 token");
                errorResult.put("error", "missing_authentication");
                return errorResult;
            }
            
            // Validate base path format
            if (!basePath.startsWith("http://") && !basePath.startsWith("https://")) {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("message", "Confluence base path must start with http:// or https://");
                errorResult.put("error", "invalid_base_path_format");
                return errorResult;
            }
            
            // Use Confluence client (similar to Jira test) instead of hardcoded HTTP client
            Confluence confluenceClient = new Confluence(basePath, token);
            confluenceClient.setAuthType(authType);
            // Disable caching for the test
            confluenceClient.setClearCache(true);
            confluenceClient.setCacheGetRequestsEnabled(false);
            // Test connection by getting current user profile
            String userProfileResponse = confluenceClient.profile();
            
            // If we get here, the connection test was successful
            Map<String, Object> successResult = new HashMap<>();
            successResult.put("success", true);
            successResult.put("message", "Confluence connection test successful");
            
            // Add user information (raw JSON response)
            Map<String, Object> userDetails = new HashMap<>();
            userDetails.put("response", userProfileResponse);
            successResult.put("user", userDetails);
            
            // Add configuration info
            Map<String, Object> configInfo = new HashMap<>();
            configInfo.put("basePath", basePath);
            successResult.put("configuration", configInfo);
            
            return successResult;
            
        } catch (IOException e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            
            String errorMessage = e.getMessage();
            if (errorMessage != null) {
                // Check for common error patterns
                if (errorMessage.contains("401") || errorMessage.contains("Unauthorized")) {
                    errorResult.put("message", "Confluence authentication failed - invalid token or credentials");
                    errorResult.put("error", "authentication_failed");
                } else if (errorMessage.contains("403") || errorMessage.contains("Forbidden")) {
                    errorResult.put("message", "Confluence access forbidden - insufficient permissions");
                    errorResult.put("error", "access_forbidden");
                } else if (errorMessage.contains("404") || errorMessage.contains("Not Found")) {
                    errorResult.put("message", "Confluence instance not found - check base path");
                    errorResult.put("error", "instance_not_found");
                } else if (errorMessage.contains("timeout") || errorMessage.contains("ConnectException")) {
                    errorResult.put("message", "Connection timeout - Confluence instance may be unreachable");
                    errorResult.put("error", "connection_timeout");
                } else {
                    errorResult.put("message", "Failed to connect to Confluence: " + errorMessage);
                    errorResult.put("error", "connection_failed");
                }
            } else {
                errorResult.put("message", "Failed to connect to Confluence with unknown error");
                errorResult.put("error", "unknown_error");
            }
            
            return errorResult;
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "Failed to connect to Confluence: " + e.getMessage());
            errorResult.put("error", "unexpected_error");
            return errorResult;
        }
    }

    /**
     * Tests a Figma integration with the provided configuration parameters.
     *
     * @param configParams A map containing the configuration parameters for the test.
     *                     Expected keys are: FIGMA_TOKEN.
     * @return A map containing the result of the connection test.
     */
    private Map<String, Object> testFigmaIntegration(Map<String, String> configParams) {
        try {
            String token = configParams.get("FIGMA_TOKEN");
            String basePath = configParams.getOrDefault("FIGMA_BASE_PATH", "https://api.figma.com");
            
            // Validate required parameters
            if (token == null || token.trim().isEmpty()) {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("message", "Figma token is required");
                errorResult.put("error", "missing_token");
                return errorResult;
            }
            
            // Normalize base path to handle all variants:
            // https://api.figma.com       → https://api.figma.com/v1
            // https://api.figma.com/      → https://api.figma.com/v1
            // https://api.figma.com/v1    → https://api.figma.com/v1
            // https://api.figma.com/v1/   → https://api.figma.com/v1
            basePath = basePath.trim();
            
            // Remove trailing slashes
            while (basePath.endsWith("/")) {
                basePath = basePath.substring(0, basePath.length() - 1);
            }
            
            // Add /v1 if not present
            if (!basePath.endsWith("/v1")) {
                basePath = basePath + "/v1";
            }
            
            // Create a basic HTTP client to test connection
            HttpClient httpClient = HttpClient.newHttpClient();
            
            // Test basic connectivity to Figma
            HttpRequest testRequest = HttpRequest.newBuilder()
                .uri(URI.create(basePath + "/me"))
                .header("X-Figma-Token", token)
                .header("Accept", "application/json")
                .GET()
                .build();
            
            HttpResponse<String> response = httpClient.send(testRequest, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                // Parse response to get user info
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode userInfo = objectMapper.readTree(response.body());
                
                Map<String, Object> successResult = new HashMap<>();
                successResult.put("success", true);
                successResult.put("message", "Figma connection test successful");
                
                // Add user information
                Map<String, Object> userDetails = new HashMap<>();
                userDetails.put("id", userInfo.path("id").asText());
                userDetails.put("handle", userInfo.path("handle").asText());
                if (userInfo.has("email")) {
                    userDetails.put("email", userInfo.path("email").asText());
                }
                successResult.put("user", userDetails);
                
                // Add configuration info
                Map<String, Object> configInfo = new HashMap<>();
                configInfo.put("basePath", basePath);
                successResult.put("configuration", configInfo);
                
                return successResult;
            } else {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                
                if (response.statusCode() == 401) {
                    errorResult.put("message", "Figma authentication failed - invalid token");
                    errorResult.put("error", "authentication_failed");
                } else if (response.statusCode() == 403) {
                    errorResult.put("message", "Figma access forbidden - insufficient permissions");
                    errorResult.put("error", "access_forbidden");
                } else if (response.statusCode() == 404) {
                    errorResult.put("message", "Figma API endpoint not found");
                    errorResult.put("error", "endpoint_not_found");
                } else {
                    errorResult.put("message", "Figma connection failed with status: " + response.statusCode());
                    errorResult.put("error", "connection_failed");
                }
                
                return errorResult;
            }
            
        } catch (IOException e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "Figma connection failed: " + e.getMessage());
            errorResult.put("error", "io_error");
            return errorResult;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "Figma connection test was interrupted");
            errorResult.put("error", "interrupted");
            return errorResult;
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "Unexpected error during Figma connection test: " + e.getMessage());
            errorResult.put("error", "unexpected_error");
            return errorResult;
        }
    }

    /**
     * Get available integration types.
     * Loads integration types from JSON configuration files.
     *
     * @return List of integration type DTOs
     */
    public List<IntegrationTypeDto> getAvailableIntegrationTypes() {
        return configurationLoader.getAllIntegrationTypes();
    }

    /**
     * Get the schema for a specific integration type.
     *
     * @param type The integration type
     * @return The integration type DTO
     */
    public IntegrationTypeDto getIntegrationTypeSchema(String type) {
        return configurationLoader.getIntegrationType(type);
    }

    /**
     * Get setup documentation content for a specific integration type.
     *
     * @param type The integration type
     * @param locale The locale for documentation
     * @return The documentation content as markdown text
     */
    public String getIntegrationDocumentation(String type, String locale) {
        IntegrationTypeDto integrationTypeSchema = configurationLoader.getIntegrationType(type, locale);
        String documentationUrl = integrationTypeSchema.getSetupDocumentationUrl();
        
        if (documentationUrl == null || documentationUrl.trim().isEmpty()) {
            throw new RuntimeException("No documentation available for integration type: " + type);
        }
        
        // Remove leading slash if present and ensure the path is relative to resources
        String resourcePath = documentationUrl.startsWith("/") ? documentationUrl.substring(1) : documentationUrl;
        
        try {
            // Load the documentation file from classpath resources
            ClassPathResource resource = new ClassPathResource(resourcePath);
            if (!resource.exists()) {
                throw new RuntimeException("Documentation file not found: " + resourcePath);
            }
            
            // Read the content as string
            try (InputStream inputStream = resource.getInputStream()) {
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read documentation file: " + resourcePath, e);
        }
    }
} 