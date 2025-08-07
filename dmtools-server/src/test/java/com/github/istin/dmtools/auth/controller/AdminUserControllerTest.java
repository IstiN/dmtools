package com.github.istin.dmtools.auth.controller;

import com.github.istin.dmtools.auth.model.User;
import com.github.istin.dmtools.auth.service.RoleService;
import com.github.istin.dmtools.auth.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AdminUserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private AdminUserController adminUserController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetUsers_Success() {
        // Arrange
        User user1 = new User();
        user1.setId("user1");
        user1.setEmail("user1@test.com");
        user1.setName("User One");
        user1.setRoles(Set.of("REGULAR_USER"));

        User user2 = new User();
        user2.setId("user2");
        user2.setEmail("admin@test.com");
        user2.setName("Admin User");
        user2.setRoles(Set.of("ADMIN"));

        List<User> users = Arrays.asList(user1, user2);
        Page<User> userPage = new PageImpl<>(users);

        when(userService.getUsers(any(Pageable.class), anyString())).thenReturn(userPage);
        when(userService.getUserRole(user1)).thenReturn("REGULAR_USER");
        when(userService.getUserRole(user2)).thenReturn("ADMIN");

        // Act
        ResponseEntity<?> response = adminUserController.getUsers(0, 50, "test");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertTrue(responseBody.containsKey("users"));
        assertTrue(responseBody.containsKey("pagination"));

        verify(userService).getUsers(any(Pageable.class), eq("test"));
    }

    @Test
    void testGetUsers_InvalidPage() {
        // Act
        ResponseEntity<?> response = adminUserController.getUsers(-1, 50, null);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("INVALID_PAGE", responseBody.get("code"));
    }

    @Test
    void testGetUsers_InvalidSize() {
        // Act
        ResponseEntity<?> response = adminUserController.getUsers(0, 0, null);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("INVALID_SIZE", responseBody.get("code"));
    }

    @Test
    void testUpdateUserRole_Success() {
        // Arrange
        String userId = "user123";
        String newRole = "ADMIN";
        
        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setEmail("user@test.com");
        updatedUser.setRoles(Set.of("ADMIN"));

        when(roleService.updateUserRole(userId, newRole)).thenReturn(updatedUser);
        when(userService.getUserRole(updatedUser)).thenReturn("ADMIN");

        Map<String, String> request = Map.of("role", newRole);

        // Act
        ResponseEntity<?> response = adminUserController.updateUserRole(userId, request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertTrue((Boolean) responseBody.get("success"));
        assertEquals("Role updated successfully", responseBody.get("message"));

        verify(roleService).updateUserRole(userId, newRole);
    }

    @Test
    void testUpdateUserRole_MissingRole() {
        // Arrange
        String userId = "user123";
        Map<String, String> request = Map.of();

        // Act
        ResponseEntity<?> response = adminUserController.updateUserRole(userId, request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("MISSING_ROLE", responseBody.get("code"));
    }

    @Test
    void testUpdateUserRole_InvalidRole() {
        // Arrange
        String userId = "user123";
        Map<String, String> request = Map.of("role", "INVALID_ROLE");

        // Act
        ResponseEntity<?> response = adminUserController.updateUserRole(userId, request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("INVALID_ROLE", responseBody.get("code"));
    }

    @Test
    void testUpdateUserRole_UserNotFound() {
        // Arrange
        String userId = "nonexistent";
        String role = "ADMIN";
        Map<String, String> request = Map.of("role", role);

        when(roleService.updateUserRole(userId, role))
                .thenThrow(new IllegalArgumentException("User not found with ID: " + userId));

        // Act
        ResponseEntity<?> response = adminUserController.updateUserRole(userId, request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("INVALID_REQUEST", responseBody.get("code"));
    }

    @Test
    void testGetCacheStats_Success() {
        // Arrange
        Map<String, Object> expectedStats = Map.of("cacheSize", 5, "cachedUserIds", Set.of("user1", "user2"));
        when(roleService.getCacheStats()).thenReturn(expectedStats);

        // Act
        ResponseEntity<?> response = adminUserController.getCacheStats();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedStats, response.getBody());

        verify(roleService).getCacheStats();
    }

    @Test
    void testClearCache_Success() {
        // Act
        ResponseEntity<?> response = adminUserController.clearCache();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertTrue((Boolean) responseBody.get("success"));
        assertEquals("Role cache cleared successfully", responseBody.get("message"));

        verify(roleService).clearCache();
    }
}
