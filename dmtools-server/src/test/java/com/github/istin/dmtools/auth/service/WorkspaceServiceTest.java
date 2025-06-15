package com.github.istin.dmtools.auth.service;

import com.github.istin.dmtools.auth.model.User;
import com.github.istin.dmtools.auth.model.Workspace;
import com.github.istin.dmtools.auth.model.WorkspaceRole;
import com.github.istin.dmtools.auth.model.WorkspaceUser;
import com.github.istin.dmtools.auth.repository.UserRepository;
import com.github.istin.dmtools.auth.repository.WorkspaceRepository;
import com.github.istin.dmtools.auth.repository.WorkspaceUserRepository;
import com.github.istin.dmtools.dto.CreateWorkspaceRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WorkspaceServiceTest {

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private WorkspaceUserRepository workspaceUserRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WorkspaceService workspaceService;

    private User owner;
    private User otherUser;
    private User nonOwner;
    private Workspace workspace;
    private WorkspaceUser workspaceUser;

    @BeforeEach
    void setUp() {
        // Enable lenient mocking to avoid UnnecessaryStubbingException
        MockitoAnnotations.openMocks(this);
        lenient().when(workspaceRepository.findById(anyString())).thenReturn(Optional.empty());
        lenient().when(workspaceRepository.findByIdAndOwnerOrUsers_User(anyString(), any(User.class), any(User.class)))
                .thenReturn(Optional.empty());

        owner = new User();
        owner.setId("owner123");
        owner.setEmail("owner@example.com");
        owner.setName("Owner");

        otherUser = new User();
        otherUser.setId("other123");
        otherUser.setEmail("other@example.com");
        otherUser.setName("Other User");

        nonOwner = new User();
        nonOwner.setId("nonowner123");
        nonOwner.setEmail("nonowner@example.com");
        nonOwner.setName("Non Owner");

        workspace = new Workspace();
        workspace.setId("1");
        workspace.setName("Test Workspace");
        workspace.setDescription("Test Description");
        workspace.setOwner(owner);
        workspace.setCreatedAt(LocalDateTime.now());
        workspace.setUpdatedAt(LocalDateTime.now());
        workspace.setUsers(new HashSet<>());

        workspaceUser = new WorkspaceUser();
        workspaceUser.setId("wu1");
        workspaceUser.setWorkspace(workspace);
        workspaceUser.setUser(otherUser);
        workspaceUser.setRole(WorkspaceRole.USER);
        workspaceUser.setJoinedAt(LocalDateTime.now());
        workspaceUser.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateWorkspace() {
        when(workspaceRepository.findByNameAndOwner(anyString(), any(User.class)))
                .thenReturn(Optional.empty());
        when(workspaceRepository.save(any(Workspace.class))).thenReturn(workspace);

        Workspace result = workspaceService.createWorkspace("Test Workspace", owner);

        assertNotNull(result);
        assertEquals("Test Workspace", result.getName());
        assertEquals(owner, result.getOwner());
        assertNotNull(result.getId());
        verify(workspaceRepository).save(any(Workspace.class));
    }

    @Test
    void testGetUserWorkspaces() {
        when(workspaceRepository.findByOwnerOrUsers_User(owner, owner))
                .thenReturn(Collections.singletonList(workspace));

        List<Workspace> result = workspaceService.getUserWorkspaces(owner);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Workspace", result.get(0).getName());
        verify(workspaceRepository).findByOwnerOrUsers_User(owner, owner);
    }

    @Test
    void testGetWorkspaceByIdForUser_Found() {
        when(workspaceRepository.findByIdAndOwnerOrUsers_User("1", owner, owner))
                .thenReturn(Optional.of(workspace));

        Optional<Workspace> result = workspaceService.getWorkspaceByIdForUser("1", owner);

        assertTrue(result.isPresent());
        assertEquals("Test Workspace", result.get().getName());
        verify(workspaceRepository).findByIdAndOwnerOrUsers_User("1", owner, owner);
    }

    @Test
    void testGetWorkspaceByIdForUser_NotFound() {
        when(workspaceRepository.findByIdAndOwnerOrUsers_User("999", owner, owner))
                .thenReturn(Optional.empty());
        
        Optional<Workspace> result = workspaceService.getWorkspaceByIdForUser("999", owner);
        
        assertFalse(result.isPresent());
    }

    @Test
    void testUpdateWorkspace() {
        CreateWorkspaceRequest request = new CreateWorkspaceRequest();
        request.setName("Updated Name");
        request.setDescription("Updated Description");
        
        when(workspaceRepository.findByIdAndOwnerOrUsers_User("1", owner, owner))
                .thenReturn(Optional.of(workspace));
        when(workspaceRepository.save(any(Workspace.class))).thenReturn(workspace);
        
        Workspace result = workspaceService.updateWorkspace("1", owner, request);
        
        assertEquals("Updated Name", result.getName());
        assertEquals("Updated Description", result.getDescription());
        verify(workspaceRepository).save(workspace);
    }

    @Test
    void testUpdateWorkspace_NotFound() {
        CreateWorkspaceRequest request = new CreateWorkspaceRequest();
        request.setName("Updated Name");
        
        when(workspaceRepository.findByIdAndOwnerOrUsers_User("999", owner, owner))
                .thenReturn(Optional.empty());
        
        assertThrows(RuntimeException.class, () -> 
            workspaceService.updateWorkspace("999", owner, request)
        );
    }

    @Test
    void testUpdateWorkspace_NotOwner() {
        CreateWorkspaceRequest request = new CreateWorkspaceRequest();
        request.setName("Updated Name");
        
        Workspace nonOwnedWorkspace = new Workspace();
        nonOwnedWorkspace.setId("1");
        nonOwnedWorkspace.setName("Test");
        nonOwnedWorkspace.setOwner(otherUser);
        
        when(workspaceRepository.findByIdAndOwnerOrUsers_User("1", owner, owner))
                .thenReturn(Optional.of(nonOwnedWorkspace));
        
        assertThrows(RuntimeException.class, () -> 
            workspaceService.updateWorkspace("1", owner, request)
        );
    }

    @Test
    void testShareWorkspace() throws IllegalAccessException {
        when(workspaceRepository.findById("1"))
                .thenReturn(Optional.of(workspace));
        when(userRepository.findByEmail(otherUser.getEmail()))
                .thenReturn(Optional.of(otherUser));
        when(workspaceUserRepository.findByWorkspaceAndUser(workspace, otherUser))
                .thenReturn(Optional.empty());
        
        WorkspaceUser newWorkspaceUser = new WorkspaceUser();
        newWorkspaceUser.setWorkspace(workspace);
        newWorkspaceUser.setUser(otherUser);
        newWorkspaceUser.setRole(WorkspaceRole.USER);
        
        when(workspaceUserRepository.save(any(WorkspaceUser.class)))
                .thenReturn(newWorkspaceUser);

        WorkspaceUser result = workspaceService.shareWorkspace("1", otherUser.getEmail(), WorkspaceRole.USER, owner);

        assertNotNull(result);
        assertEquals(otherUser, result.getUser());
        assertEquals(WorkspaceRole.USER, result.getRole());
        verify(workspaceUserRepository).save(any(WorkspaceUser.class));
    }

    @Test
    void testShareWorkspace_WorkspaceNotFound() {
        when(workspaceRepository.findById("999"))
                .thenReturn(Optional.empty());
        
        assertThrows(IllegalArgumentException.class, () -> 
            workspaceService.shareWorkspace("999", "other@example.com", WorkspaceRole.USER, owner)
        );
    }

    @Test
    void testShareWorkspace_UserNotFound() {
        when(workspaceRepository.findById("1"))
                .thenReturn(Optional.of(workspace));
        when(userRepository.findByEmail("nonexistent@example.com"))
                .thenReturn(Optional.empty());
        
        assertThrows(IllegalArgumentException.class, () -> 
            workspaceService.shareWorkspace("1", "nonexistent@example.com", WorkspaceRole.USER, owner)
        );
    }

    @Test
    void testShareWorkspace_NotOwner() {
        when(workspaceRepository.findById("1"))
                .thenReturn(Optional.of(workspace));
        
        assertThrows(IllegalAccessException.class, () -> 
            workspaceService.shareWorkspace("1", "other@example.com", WorkspaceRole.USER, nonOwner)
        );
    }

    @Test
    void testRemoveUserFromWorkspace() throws IllegalAccessException {
        when(workspaceRepository.findById("1"))
                .thenReturn(Optional.of(workspace));
        when(workspaceUserRepository.findByWorkspaceAndUser(workspace, otherUser))
                .thenReturn(Optional.of(workspaceUser));
        
        workspaceService.removeUserFromWorkspace("1", owner, otherUser);
        
        verify(workspaceUserRepository).delete(workspaceUser);
    }

    @Test
    void testRemoveUserFromWorkspace_WorkspaceNotFound() {
        when(workspaceRepository.findById("999"))
                .thenReturn(Optional.empty());
        
        assertThrows(IllegalArgumentException.class, () -> 
            workspaceService.removeUserFromWorkspace("999", owner, otherUser)
        );
    }

    @Test
    void testRemoveUserFromWorkspace_UserNotInWorkspace() {
        when(workspaceRepository.findById("1"))
                .thenReturn(Optional.of(workspace));
        when(workspaceUserRepository.findByWorkspaceAndUser(workspace, otherUser))
                .thenReturn(Optional.empty());
        
        assertThrows(IllegalArgumentException.class, () -> 
            workspaceService.removeUserFromWorkspace("1", owner, otherUser)
        );
    }

    @Test
    void testRemoveUserFromWorkspace_NotOwner() {
        when(workspaceRepository.findById("1"))
                .thenReturn(Optional.of(workspace));
        
        assertThrows(IllegalAccessException.class, () -> 
            workspaceService.removeUserFromWorkspace("1", nonOwner, otherUser)
        );
    }

    @Test
    void testRemoveUserFromWorkspace_CannotRemoveOwner() {
        when(workspaceRepository.findById("1"))
                .thenReturn(Optional.of(workspace));
        
        assertThrows(IllegalArgumentException.class, () -> 
            workspaceService.removeUserFromWorkspace("1", owner, owner)
        );
    }

    @Test
    void testDeleteWorkspace() {
        when(workspaceRepository.findByIdAndOwnerOrUsers_User("1", owner, owner))
                .thenReturn(Optional.of(workspace));
        doNothing().when(workspaceRepository).delete(any(Workspace.class));

        boolean result = workspaceService.deleteWorkspace("1", owner);

        assertTrue(result);
        verify(workspaceRepository).delete(workspace);
    }

    @Test
    void testDeleteWorkspace_NotFound() {
        when(workspaceRepository.findByIdAndOwnerOrUsers_User("999", owner, owner))
                .thenReturn(Optional.empty());
        
        boolean result = workspaceService.deleteWorkspace("999", owner);
        
        assertFalse(result);
        verify(workspaceRepository, never()).delete(any());
    }

    @Test
    void testDeleteWorkspace_NotOwner() {
        Workspace nonOwnedWorkspace = new Workspace();
        nonOwnedWorkspace.setId("1");
        nonOwnedWorkspace.setName("Test Workspace");
        nonOwnedWorkspace.setOwner(otherUser);
        
        when(workspaceRepository.findByIdAndOwnerOrUsers_User("1", owner, owner))
                .thenReturn(Optional.of(nonOwnedWorkspace));
        
        assertThrows(IllegalStateException.class, () -> 
            workspaceService.deleteWorkspace("1", owner)
        );
    }

    @Test
    void testCreateDefaultWorkspace() {
        when(workspaceRepository.findByNameAndOwner(anyString(), any(User.class)))
                .thenReturn(Optional.empty());
        when(workspaceRepository.save(any(Workspace.class)))
                .thenAnswer(i -> {
                    Workspace w = i.getArgument(0);
                    w.setId("default-id");
                    return w;
                });
        
        Workspace result = workspaceService.createDefaultWorkspace(owner);
        
        assertNotNull(result);
        assertEquals("Default Workspace", result.getName());
        assertEquals("Your default workspace", result.getDescription());
        assertEquals(owner, result.getOwner());
    }

    @Test
    void testUuidConversionError() {
        when(workspaceRepository.findByNameAndOwner(anyString(), any(User.class)))
                .thenReturn(Optional.empty());
        
        when(workspaceRepository.save(any(Workspace.class)))
                .thenThrow(new DataIntegrityViolationException(
                        "Could not extract column [1] from JDBC ResultSet [Data conversion error converting \"240e22ca-e410-4807-ae66-c48bb4ebeb34\" [22018-224]] [n/a]"));

        Exception exception = assertThrows(DataIntegrityViolationException.class, () -> {
            workspaceService.createWorkspace("Test Workspace", owner);
        });

        assertTrue(exception.getMessage().contains("Data conversion error"));
    }
} 