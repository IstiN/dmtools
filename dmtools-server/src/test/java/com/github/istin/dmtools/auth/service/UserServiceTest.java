package com.github.istin.dmtools.auth.service;

import com.github.istin.dmtools.auth.model.User;
import com.github.istin.dmtools.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userRepository);
        
        // Set admin emails for testing
        ReflectionTestUtils.setField(userService, "adminEmailsList", "admin@test.com,super@test.com");
        userService.initializeAdminEmails();
    }

    @Test
    void testIsAdminEmail_ValidAdmin() {
        // Act & Assert
        assertTrue(userService.isAdminEmail("admin@test.com"));
        assertTrue(userService.isAdminEmail("super@test.com"));
    }

    @Test
    void testIsAdminEmail_NotAdmin() {
        // Act & Assert
        assertFalse(userService.isAdminEmail("user@test.com"));
        assertFalse(userService.isAdminEmail(""));
        assertFalse(userService.isAdminEmail(null));
    }

    @Test
    void testGetUsers_WithSearch() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        String search = "john";
        
        User user1 = new User();
        user1.setEmail("john@test.com");
        user1.setName("John Doe");
        
        List<User> users = Arrays.asList(user1);
        Page<User> userPage = new PageImpl<>(users, pageable, 1);
        
        when(userRepository.findByEmailContainingIgnoreCaseOrNameContainingIgnoreCase(search, search, pageable))
                .thenReturn(userPage);
        
        // Act
        Page<User> result = userService.getUsers(pageable, search);
        
        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals("john@test.com", result.getContent().get(0).getEmail());
        verify(userRepository).findByEmailContainingIgnoreCaseOrNameContainingIgnoreCase(search, search, pageable);
    }

    @Test
    void testGetUsers_WithoutSearch() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        
        User user1 = new User();
        user1.setEmail("user1@test.com");
        
        List<User> users = Arrays.asList(user1);
        Page<User> userPage = new PageImpl<>(users, pageable, 1);
        
        when(userRepository.findAll(pageable)).thenReturn(userPage);
        
        // Act
        Page<User> result = userService.getUsers(pageable, null);
        
        // Assert
        assertEquals(1, result.getTotalElements());
        verify(userRepository).findAll(pageable);
    }

    @Test
    void testUpdateUserRole_ValidRole() {
        // Arrange
        String userId = "user123";
        String newRole = "ADMIN";
        
        User user = new User();
        user.setId(userId);
        user.setEmail("user@test.com");
        user.setRoles(Set.of("REGULAR_USER"));
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        User result = userService.updateUserRole(userId, newRole);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getRoles().contains("ADMIN"));
        assertFalse(result.getRoles().contains("REGULAR_USER"));
        verify(userRepository).save(user);
    }

    @Test
    void testUpdateUserRole_InvalidRole() {
        // Arrange
        String userId = "user123";
        String invalidRole = "INVALID_ROLE";
        
        User user = new User();
        user.setId(userId);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.updateUserRole(userId, invalidRole));
        
        assertTrue(exception.getMessage().contains("Invalid role"));
    }

    @Test
    void testUpdateUserRole_UserNotFound() {
        // Arrange
        String userId = "nonexistent";
        String role = "ADMIN";
        
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.updateUserRole(userId, role));
        
        assertTrue(exception.getMessage().contains("User not found"));
    }

    @Test
    void testGetUserRole_AdminUser() {
        // Arrange
        User user = new User();
        user.setRoles(Set.of("ADMIN", "REGULAR_USER"));
        
        // Act
        String result = userService.getUserRole(user);
        
        // Assert
        assertEquals("ADMIN", result);
    }

    @Test
    void testGetUserRole_RegularUser() {
        // Arrange
        User user = new User();
        user.setRoles(Set.of("REGULAR_USER"));
        
        // Act
        String result = userService.getUserRole(user);
        
        // Assert
        assertEquals("REGULAR_USER", result);
    }

    @Test
    void testGetUserRole_NoRoles() {
        // Arrange
        User user = new User();
        user.setRoles(null);
        
        // Act
        String result = userService.getUserRole(user);
        
        // Assert
        assertEquals("REGULAR_USER", result);
    }

    @Test
    void testHasAdminRole_True() {
        // Arrange
        User user = new User();
        user.setRoles(Set.of("ADMIN"));
        
        // Act
        boolean result = userService.hasAdminRole(user);
        
        // Assert
        assertTrue(result);
    }

    @Test
    void testHasAdminRole_False() {
        // Arrange
        User user = new User();
        user.setRoles(Set.of("REGULAR_USER"));
        
        // Act
        boolean result = userService.hasAdminRole(user);
        
        // Assert
        assertFalse(result);
    }
}
