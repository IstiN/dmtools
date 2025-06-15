package com.github.istin.dmtools.server;

import com.github.istin.dmtools.auth.model.User;
import com.github.istin.dmtools.auth.model.Workspace;
import com.github.istin.dmtools.auth.model.WorkspaceRole;
import com.github.istin.dmtools.auth.model.WorkspaceUser;
import com.github.istin.dmtools.auth.service.UserService;
import com.github.istin.dmtools.auth.service.WorkspaceService;
import com.github.istin.dmtools.dto.CreateWorkspaceRequest;
import com.github.istin.dmtools.dto.ShareWorkspaceRequest;
import com.github.istin.dmtools.dto.WorkspaceDto;
import com.github.istin.dmtools.dto.WorkspaceUserDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class WorkspaceControllerTest {

    private MockMvc mockMvc;

    @Mock
    private WorkspaceService workspaceService;

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private WorkspaceController workspaceController;

    private ObjectMapper objectMapper = new ObjectMapper();

    private User testUser;
    private User otherUser;
    private Workspace testWorkspace;
    private WorkspaceUser testWorkspaceUser;

    @BeforeEach
    void setUp() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);
        
        mockMvc = MockMvcBuilders.standaloneSetup(workspaceController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        testUser = new User();
        testUser.setId("user123");
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");

        otherUser = new User();
        otherUser.setId("user2");
        otherUser.setEmail("other@example.com");
        otherUser.setName("Other User");

        testWorkspace = new Workspace();
        testWorkspace.setId("1");
        testWorkspace.setName("Test Workspace");
        testWorkspace.setDescription("Test Description");
        testWorkspace.setOwner(testUser);
        testWorkspace.setCreatedAt(LocalDateTime.now());
        testWorkspace.setUpdatedAt(LocalDateTime.now());
        testWorkspace.setUsers(new HashSet<>());

        testWorkspaceUser = new WorkspaceUser();
        testWorkspaceUser.setId("wu1");
        testWorkspaceUser.setWorkspace(testWorkspace);
        testWorkspaceUser.setUser(testUser);
        testWorkspaceUser.setRole(WorkspaceRole.ADMIN);

        Set<WorkspaceUser> users = new HashSet<>();
        users.add(testWorkspaceUser);
        testWorkspace.setUsers(users);

        when(authentication.getPrincipal()).thenReturn("test@example.com");
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
    }

    @Test
    void testCreateWorkspace() {
        // Setup
        CreateWorkspaceRequest request = new CreateWorkspaceRequest();
        request.setName("New Workspace");
        request.setDescription("New Description");

        when(workspaceService.createWorkspace(anyString(), any(User.class))).thenReturn(testWorkspace);

        // Test
        ResponseEntity<WorkspaceDto> response = workspaceController.createWorkspace(request, authentication);

        // Verify
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testWorkspace.getName(), response.getBody().getName());
        verify(workspaceService).createWorkspace(request.getName(), testUser);
    }

    @Test
    void testGetUserWorkspaces() {
        // Setup
        List<Workspace> workspaces = Collections.singletonList(testWorkspace);
        when(workspaceService.getUserWorkspaces(any(User.class))).thenReturn(workspaces);

        // Test
        ResponseEntity<List<WorkspaceDto>> response = workspaceController.getUserWorkspaces(authentication);

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(testWorkspace.getName(), response.getBody().get(0).getName());
    }

    @Test
    void testGetWorkspaceById() {
        // Setup
        when(workspaceService.getWorkspaceByIdForUser(anyString(), any(User.class)))
                .thenReturn(Optional.of(testWorkspace));

        // Test
        ResponseEntity<WorkspaceDto> response = workspaceController.getWorkspaceById("1", authentication);

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testWorkspace.getName(), response.getBody().getName());
    }

    @Test
    void testDeleteWorkspace() {
        // Setup
        when(workspaceService.deleteWorkspace(anyString(), any(User.class))).thenReturn(true);

        // Test
        ResponseEntity<Void> response = workspaceController.deleteWorkspace("1", authentication);

        // Verify
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void testShareWorkspace() throws IllegalAccessException {
        // Setup
        ShareWorkspaceRequest request = new ShareWorkspaceRequest();
        request.setEmail("user@example.com");
        request.setRole(WorkspaceRole.USER);

        User sharedUser = new User();
        sharedUser.setId("shared123");
        sharedUser.setEmail("user@example.com");

        WorkspaceUser sharedWorkspaceUser = new WorkspaceUser();
        sharedWorkspaceUser.setWorkspace(testWorkspace);
        sharedWorkspaceUser.setUser(sharedUser);
        sharedWorkspaceUser.setRole(WorkspaceRole.USER);

        when(workspaceService.shareWorkspace(anyString(), anyString(), any(WorkspaceRole.class), any(User.class)))
                .thenReturn(sharedWorkspaceUser);

        // Test
        ResponseEntity<?> response = workspaceController.shareWorkspace("1", request, authentication);

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof WorkspaceUserDto);
        assertEquals(sharedUser.getEmail(), ((WorkspaceUserDto) response.getBody()).getEmail());
    }

    @Test
    void testCreateDefaultWorkspace() {
        // Setup
        when(workspaceService.createDefaultWorkspace(any(User.class))).thenReturn(testWorkspace);

        // Test
        ResponseEntity<WorkspaceDto> response = workspaceController.createDefaultWorkspace(authentication);

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testWorkspace.getName(), response.getBody().getName());
    }

    @Test
    void testGetWorkspaceById_NotFound() {
        // Setup
        when(workspaceService.getWorkspaceByIdForUser("999", testUser)).thenReturn(Optional.empty());

        // Test
        ResponseEntity<WorkspaceDto> response = workspaceController.getWorkspaceById("999", authentication);

        // Verify
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(workspaceService).getWorkspaceByIdForUser("999", testUser);
    }

    @Test
    void testDeleteWorkspace_NotFound() {
        // Setup
        when(workspaceService.deleteWorkspace("999", testUser)).thenReturn(false);

        // Test
        ResponseEntity<Void> response = workspaceController.deleteWorkspace("999", authentication);

        // Verify
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(workspaceService).deleteWorkspace("999", testUser);
    }

    @Test
    void testUndefinedWorkspaceIdError() throws Exception {
        // This test verifies that the controller properly handles invalid workspace IDs
        // We'll test this by making a request to a URL with "undefined" as the ID
        
        when(workspaceService.getWorkspaceByIdForUser("undefined", testUser))
            .thenThrow(new IllegalArgumentException("Invalid ID: 'undefined' is not a valid identifier"));
        
        ResponseEntity<WorkspaceDto> response = workspaceController.getWorkspaceById("undefined", authentication);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
} 