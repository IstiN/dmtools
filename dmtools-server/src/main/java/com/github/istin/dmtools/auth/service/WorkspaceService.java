package com.github.istin.dmtools.auth.service;

import com.github.istin.dmtools.auth.model.User;
import com.github.istin.dmtools.auth.model.Workspace;
import com.github.istin.dmtools.auth.model.WorkspaceRole;
import com.github.istin.dmtools.auth.model.WorkspaceUser;
import com.github.istin.dmtools.auth.repository.UserRepository;
import com.github.istin.dmtools.auth.repository.WorkspaceRepository;
import com.github.istin.dmtools.auth.repository.WorkspaceUserRepository;
import com.github.istin.dmtools.dto.CreateWorkspaceRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceUserRepository workspaceUserRepository;
    private final UserRepository userRepository;

    public Workspace createWorkspace(String workspaceName, User owner) {
        return createWorkspace(workspaceName, "", owner);
    }

    public Workspace createWorkspace(String workspaceName, String description, User owner) {
        // Check for duplicate workspace names for this owner
        if (workspaceRepository.findByNameAndOwner(workspaceName, owner).isPresent()) {
            throw new IllegalArgumentException("A workspace with this name already exists.");
        }
        
        // Create a new workspace with proper initialization
        Workspace workspace = new Workspace();
        workspace.setName(workspaceName);
        workspace.setOwner(owner);
        workspace.setDescription(description != null ? description : ""); // Set description
        
        try {
            return workspaceRepository.save(workspace);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Log the error for debugging
            String errorMessage = "Error creating workspace: " + e.getMessage();
            System.err.println(errorMessage);
            
            // Check if it's the UUID conversion error
            if (e.getMessage() != null && e.getMessage().contains("Data conversion error converting")) {
                throw new IllegalStateException("Database ID type mismatch. Please check entity ID types.", e);
            }
            
            // Rethrow the original exception if it's not the specific error we're handling
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<Workspace> getUserWorkspaces(User user) {
        return workspaceRepository.findByOwnerOrUsers_User(user, user);
    }

    @Transactional(readOnly = true)
    public Optional<Workspace> getWorkspaceByIdForUser(String workspaceId, User user) {
        return workspaceRepository.findByIdAndOwnerOrUsers_User(workspaceId, user, user);
    }

    public Workspace updateWorkspace(String workspaceId, User user, CreateWorkspaceRequest request) {
        Workspace workspace = getWorkspaceByIdForUser(workspaceId, user)
                .orElseThrow(() -> new RuntimeException("Workspace not found or user does not have access"));

        if (!workspace.getOwner().equals(user)) {
            throw new RuntimeException("Only workspace owner can update workspace");
        }

        workspace.setName(request.getName());
        workspace.setDescription(request.getDescription());
        return workspaceRepository.save(workspace);
    }

    public boolean deleteWorkspace(String workspaceId, User user) {
        Optional<Workspace> workspaceOpt = getWorkspaceByIdForUser(workspaceId, user);
        if (workspaceOpt.isEmpty()) {
            return false;
        }
        Workspace workspace = workspaceOpt.get();
        if (!workspace.getOwner().equals(user)) {
            // This check is technically redundant due to getWorkspaceByIdForUser, but good for safety
            throw new IllegalStateException("Only the workspace owner can delete the workspace.");
        }
        workspaceRepository.delete(workspace);
        return true;
    }

    public WorkspaceUser shareWorkspace(String workspaceId, String sharedWithUserEmail, WorkspaceRole role, User owner) throws IllegalAccessException {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found."));

        if (!workspace.getOwner().equals(owner)) {
            throw new IllegalAccessException("Only the workspace owner can share the workspace.");
        }

        User userToShareWith = userRepository.findByEmail(sharedWithUserEmail)
                .orElseThrow(() -> new IllegalArgumentException("User to share with not found."));

        if(userToShareWith.equals(owner)) {
            throw new IllegalArgumentException("Cannot share workspace with the owner.");
        }

        WorkspaceUser workspaceUser = workspaceUserRepository.findByWorkspaceAndUser(workspace, userToShareWith)
                .orElse(new WorkspaceUser());

        workspaceUser.setWorkspace(workspace);
        workspaceUser.setUser(userToShareWith);
        workspaceUser.setRole(role);

        return workspaceUserRepository.save(workspaceUser);
    }

    public void removeUserFromWorkspace(String workspaceId, User owner, User targetUser) throws IllegalAccessException {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found."));

        if (!workspace.getOwner().equals(owner)) {
            throw new IllegalAccessException("Only the workspace owner can remove users.");
        }

        if (workspace.getOwner().equals(targetUser)) {
            throw new IllegalArgumentException("Cannot remove the workspace owner.");
        }

        WorkspaceUser workspaceUser = workspaceUserRepository.findByWorkspaceAndUser(workspace, targetUser)
                .orElseThrow(() -> new IllegalArgumentException("User is not in this workspace."));

        workspaceUserRepository.delete(workspaceUser);
    }

     public Workspace createDefaultWorkspace(User user) {
        CreateWorkspaceRequest request = new CreateWorkspaceRequest();
        request.setName("Default Workspace");
        request.setDescription("Your default workspace");

        return createWorkspace(request.getName(), user);
    }

} 