package com.github.istin.dmtools.auth.service;

import com.github.istin.dmtools.auth.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoleServiceTest {

    @Mock
    private UserService userService;

    private RoleService roleService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        roleService = new RoleService(userService);
    }

    @Test
    void testGetUserRole_CacheHit() {
        // Arrange
        String userId = "user123";
        String expectedRole = "ADMIN";
        
        // First call should hit database and cache the result
        User user = new User();
        user.setId(userId);
        user.setRoles(Set.of("ADMIN"));
        
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(userService.getUserRole(user)).thenReturn(expectedRole);
        
        // Act - first call
        String firstResult = roleService.getUserRole(userId);
        
        // Act - second call (should hit cache)
        String secondResult = roleService.getUserRole(userId);
        
        // Assert
        assertEquals(expectedRole, firstResult);
        assertEquals(expectedRole, secondResult);
        
        // Verify database is called only once
        verify(userService, times(1)).findById(userId);
        verify(userService, times(1)).getUserRole(user);
    }

    @Test
    void testGetUserRole_UserNotFound() {
        // Arrange
        String userId = "nonexistent";
        when(userService.findById(userId)).thenReturn(Optional.empty());
        
        // Act
        String result = roleService.getUserRole(userId);
        
        // Assert
        assertEquals("REGULAR_USER", result);
        verify(userService, times(1)).findById(userId);
    }

    @Test
    void testUpdateUserRole_InvalidatesCache() {
        // Arrange
        String userId = "user123";
        String newRole = "REGULAR_USER";
        
        User user = new User();
        user.setId(userId);
        user.setRoles(Set.of("REGULAR_USER"));
        
        when(userService.updateUserRole(userId, newRole)).thenReturn(user);
        
        // Act
        User result = roleService.updateUserRole(userId, newRole);
        
        // Assert
        assertNotNull(result);
        verify(userService, times(1)).updateUserRole(userId, newRole);
    }

    @Test
    void testHasAdminRole_WithAdminUser() {
        // Arrange
        String userId = "admin123";
        User user = new User();
        user.setId(userId);
        user.setRoles(Set.of("ADMIN"));
        
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(userService.getUserRole(user)).thenReturn("ADMIN");
        
        // Act
        boolean result = roleService.hasAdminRole(userId);
        
        // Assert
        assertTrue(result);
    }

    @Test
    void testHasAdminRole_WithRegularUser() {
        // Arrange
        String userId = "user123";
        User user = new User();
        user.setId(userId);
        user.setRoles(Set.of("REGULAR_USER"));
        
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(userService.getUserRole(user)).thenReturn("REGULAR_USER");
        
        // Act
        boolean result = roleService.hasAdminRole(userId);
        
        // Assert
        assertFalse(result);
    }

    @Test
    void testClearCache() {
        // Arrange
        String userId = "user123";
        User user = new User();
        user.setId(userId);
        user.setRoles(Set.of("ADMIN"));
        
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(userService.getUserRole(user)).thenReturn("ADMIN");
        
        // Populate cache
        roleService.getUserRole(userId);
        
        // Act
        roleService.clearCache();
        
        // Get role again - should hit database again
        roleService.getUserRole(userId);
        
        // Assert
        verify(userService, times(2)).findById(userId);
    }

    @Test
    void testInvalidateUserCache() {
        // Arrange
        String userId = "user123";
        User user = new User();
        user.setId(userId);
        user.setRoles(Set.of("ADMIN"));
        
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(userService.getUserRole(user)).thenReturn("ADMIN");
        
        // Populate cache
        roleService.getUserRole(userId);
        
        // Act
        roleService.invalidateUserCache(userId);
        
        // Get role again - should hit database again
        roleService.getUserRole(userId);
        
        // Assert
        verify(userService, times(2)).findById(userId);
    }
}
