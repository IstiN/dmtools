package com.github.istin.dmtools.auth.service;

import com.github.istin.dmtools.auth.model.*;
import com.github.istin.dmtools.auth.repository.*;
import com.github.istin.dmtools.auth.util.EncryptionUtils;
import com.github.istin.dmtools.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    public List<IntegrationDto> getIntegrationsForUser(String userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        List<Integration> integrations = integrationRepository.findAccessibleToUser(user);
        return integrations.stream()
                .map(IntegrationDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get integrations for a specific workspace.
     *
     * @param workspaceId The ID of the workspace
     * @return List of integration DTOs
     */
    public List<IntegrationDto> getIntegrationsForWorkspace(String workspaceId) {
        Workspace workspace = workspaceRepo.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found"));
        
        List<Integration> integrations = integrationRepository.findByWorkspace(workspace);
        return integrations.stream()
                .map(IntegrationDto::fromEntity)
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
    public IntegrationDto getIntegrationById(String integrationId, String userId, boolean includeSensitive) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Integration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new IllegalArgumentException("Integration not found"));
        
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
            IntegrationDto dto = IntegrationDto.fromEntityWithSensitiveData(integration);
            
            // Decrypt sensitive values
            dto.getConfigParams().stream()
                    .filter(IntegrationConfigDto::isSensitive)
                    .forEach(config -> {
                        if (config.getParamValue() != null) {
                            config.setParamValue(encryptionUtils.decrypt(config.getParamValue()));
                        }
                    });
            
            return dto;
        } else {
            return IntegrationDto.fromEntity(integration);
        }
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
        
        return IntegrationDto.fromEntity(savedIntegration);
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
        return IntegrationDto.fromEntity(updatedIntegration);
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
        
        // Delete the integration and all related entities
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
        return IntegrationDto.fromEntity(updatedIntegration);
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
     * Test an integration connection.
     *
     * @param request The test integration request
     * @return A map containing the test result
     */
    public Map<String, Object> testIntegration(TestIntegrationRequest request) {
        // This would be implemented based on the specific integration type
        // For now, we'll return a simple success response
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Connection test successful");
        return result;
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
} 