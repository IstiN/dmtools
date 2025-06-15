package com.github.istin.dmtools.auth.repository;

import com.github.istin.dmtools.auth.model.AuthProvider;
import com.github.istin.dmtools.auth.model.User;
import com.github.istin.dmtools.auth.model.Workspace;
import com.github.istin.dmtools.auth.model.WorkspaceRole;
import com.github.istin.dmtools.auth.model.WorkspaceUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class WorkspaceUserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private WorkspaceUserRepository workspaceUserRepository;

    private User owner;
    private User collaborator;
    private Workspace workspace;
    private WorkspaceUser workspaceUser;

    @BeforeEach
    void setUp() {
        // Create test users
        owner = new User();
        owner.setEmail("owner@example.com");
        owner.setName("Owner User");
        owner.setProvider(AuthProvider.GOOGLE);
        owner.setProviderId("owner123");
        entityManager.persist(owner);

        collaborator = new User();
        collaborator.setEmail("collaborator@example.com");
        collaborator.setName("Collaborator User");
        collaborator.setProvider(AuthProvider.GOOGLE);
        collaborator.setProviderId("collaborator123");
        entityManager.persist(collaborator);

        // Create workspace
        workspace = new Workspace();
        workspace.setName("Test Workspace");
        workspace.setDescription("Test Description");
        workspace.setOwner(owner);
        entityManager.persist(workspace);

        // Add collaborator to workspace
        workspaceUser = new WorkspaceUser();
        workspaceUser.setWorkspace(workspace);
        workspaceUser.setUser(collaborator);
        workspaceUser.setRole(WorkspaceRole.USER);
        entityManager.persist(workspaceUser);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void findByWorkspaceAndUser_Success() {
        // Act
        Optional<WorkspaceUser> foundWorkspaceUser = workspaceUserRepository.findByWorkspaceAndUser(workspace, collaborator);
        
        // Assert
        assertTrue(foundWorkspaceUser.isPresent());
        assertEquals(workspace.getId(), foundWorkspaceUser.get().getWorkspace().getId());
        assertEquals(collaborator.getId(), foundWorkspaceUser.get().getUser().getId());
        assertEquals(WorkspaceRole.USER, foundWorkspaceUser.get().getRole());
    }

    @Test
    void findByWorkspaceAndUser_NotFound() {
        // Create a new user not associated with the workspace
        User newUser = new User();
        newUser.setEmail("newuser@example.com");
        newUser.setName("New User");
        newUser.setProvider(AuthProvider.GOOGLE);
        newUser.setProviderId("newuser123");
        entityManager.persist(newUser);
        entityManager.flush();
        
        // Act
        Optional<WorkspaceUser> foundWorkspaceUser = workspaceUserRepository.findByWorkspaceAndUser(workspace, newUser);
        
        // Assert
        assertFalse(foundWorkspaceUser.isPresent());
    }
} 