package com.github.istin.dmtools.auth.repository;

import com.github.istin.dmtools.auth.model.AuthProvider;
import com.github.istin.dmtools.auth.model.User;
import com.github.istin.dmtools.auth.model.Workspace;
import com.github.istin.dmtools.auth.model.WorkspaceRole;
import com.github.istin.dmtools.auth.model.WorkspaceUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Disabled
@DataJpaTest
public class WorkspaceRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkspaceUserRepository workspaceUserRepository;

    private User owner;
    private User collaborator;
    private Workspace workspace1;
    private Workspace workspace2;

    @BeforeEach
    void setUp() {
        // Create test users
        owner = new User();
        owner.setEmail("owner@example.com");
        owner.setName("Owner User");
        owner.setProvider(AuthProvider.GOOGLE);
        owner.setProviderId("owner123");
        owner = userRepository.save(owner);

        collaborator = new User();
        collaborator.setEmail("collaborator@example.com");
        collaborator.setName("Collaborator User");
        collaborator.setProvider(AuthProvider.GOOGLE);
        collaborator.setProviderId("collaborator123");
        collaborator = userRepository.save(collaborator);

        // Create workspaces
        workspace1 = new Workspace();
        workspace1.setName("Workspace 1");
        workspace1.setDescription("Description 1");
        workspace1.setOwner(owner);
        workspace1 = workspaceRepository.save(workspace1);

        workspace2 = new Workspace();
        workspace2.setName("Workspace 2");
        workspace2.setDescription("Description 2");
        workspace2.setOwner(owner);
        workspace2 = workspaceRepository.save(workspace2);

        // Add collaborator to workspace1
        WorkspaceUser workspaceUser = new WorkspaceUser();
        workspaceUser.setWorkspace(workspace1);
        workspaceUser.setUser(collaborator);
        workspaceUser.setRole(WorkspaceRole.USER);
        workspaceUserRepository.save(workspaceUser);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void findByNameAndOwner_Success() {
        // Act
        Optional<Workspace> foundWorkspace = workspaceRepository.findByNameAndOwner("Workspace 1", owner);
        
        // Assert
        assertTrue(foundWorkspace.isPresent());
        assertEquals("Workspace 1", foundWorkspace.get().getName());
        assertEquals(owner.getId(), foundWorkspace.get().getOwner().getId());
    }

    @Test
    void findByNameAndOwner_NotFound() {
        // Act
        Optional<Workspace> foundWorkspace = workspaceRepository.findByNameAndOwner("Non-existent Workspace", owner);
        
        // Assert
        assertFalse(foundWorkspace.isPresent());
    }

    @Test
    void findByOwnerOrUsers_User_AsOwner() {
        // Act
        List<Workspace> workspaces = workspaceRepository.findByOwnerOrUsers_User(owner, owner);
        
        // Assert
        assertEquals(2, workspaces.size());
        assertTrue(workspaces.stream().anyMatch(w -> w.getName().equals("Workspace 1")));
        assertTrue(workspaces.stream().anyMatch(w -> w.getName().equals("Workspace 2")));
    }

    @Test
    void findByOwnerOrUsers_User_AsCollaborator() {
        // Act
        List<Workspace> workspaces = workspaceRepository.findByOwnerOrUsers_User(collaborator, collaborator);
        
        // Assert
        assertEquals(1, workspaces.size());
        assertEquals("Workspace 1", workspaces.get(0).getName());
    }

    @Test
    void findByIdAndOwnerOrUsers_User_AsOwner() {
        // Act
        Optional<Workspace> foundWorkspace = workspaceRepository.findByIdAndOwnerOrUsers_User(workspace1.getId(), owner, owner);
        
        // Assert
        assertTrue(foundWorkspace.isPresent());
        assertEquals("Workspace 1", foundWorkspace.get().getName());
    }

    @Test
    void findByIdAndOwnerOrUsers_User_AsCollaborator() {
        // Act
        Optional<Workspace> foundWorkspace = workspaceRepository.findByIdAndOwnerOrUsers_User(workspace1.getId(), collaborator, collaborator);
        
        // Assert
        assertTrue(foundWorkspace.isPresent());
        assertEquals("Workspace 1", foundWorkspace.get().getName());
    }

    @Test
    void findByIdAndOwnerOrUsers_User_NoAccess() {
        // Act
        Optional<Workspace> foundWorkspace = workspaceRepository.findByIdAndOwnerOrUsers_User(workspace2.getId(), collaborator, collaborator);
        
        // Assert
        assertFalse(foundWorkspace.isPresent());
    }
} 