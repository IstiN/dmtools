package com.github.istin.dmtools.auth.service;

import com.github.istin.dmtools.auth.model.*;
import com.github.istin.dmtools.auth.repository.*;
import com.github.istin.dmtools.auth.util.EncryptionUtils;
import com.github.istin.dmtools.dto.CreateIntegrationRequest;
import com.github.istin.dmtools.dto.IntegrationDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IntegrationServiceTest {

    @Mock
    private IntegrationRepository integrationRepository;
    @Mock
    private IntegrationConfigRepository configRepository;
    @Mock
    private IntegrationUserRepository userRepository;
    @Mock
    private IntegrationWorkspaceRepository workspaceRepository;
    @Mock
    private UserRepository userRepo;
    @Mock
    private WorkspaceRepository workspaceRepo;
    @Mock
    private EncryptionUtils encryptionUtils;

    @InjectMocks
    private IntegrationService integrationService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId("user1");
        user.setEmail("test@example.com");
        user.setName("Test User");
    }

    @Test
    void createIntegration_ShouldCreateIntegration() {
        // Arrange
        CreateIntegrationRequest request = new CreateIntegrationRequest();
        request.setName("Test Integration");
        request.setType("jira");

        when(userRepo.findById("user1")).thenReturn(Optional.of(user));
        when(integrationRepository.save(any(Integration.class))).thenAnswer(i -> {
            Integration integration = i.getArgument(0);
            integration.setId("int1");
            return integration;
        });

        // Act
        IntegrationDto result = integrationService.createIntegration(request, "user1");

        // Assert
        assertNotNull(result);
        assertEquals("Test Integration", result.getName());
        assertEquals("jira", result.getType());
        verify(integrationRepository, times(1)).save(any(Integration.class));
    }

    @Test
    void getIntegrationById_WhenUserIsCreator_ShouldReturnDto() {
        // Arrange
        Integration integration = new Integration();
        integration.setId("int1");
        integration.setCreatedBy(user);
        integration.setUsers(Collections.emptySet());

        when(userRepo.findById("user1")).thenReturn(Optional.of(user));
        when(integrationRepository.findById("int1")).thenReturn(Optional.of(integration));

        // Act
        IntegrationDto result = integrationService.getIntegrationById("int1", "user1", false);

        // Assert
        assertNotNull(result);
        assertEquals("int1", result.getId());
    }

    @Test
    void getIntegrationById_WhenUserNotAuthorized_ShouldThrowException() {
        // Arrange
        Integration integration = new Integration();
        integration.setId("int1");
        User anotherUser = new User();
        anotherUser.setId("user2");
        integration.setCreatedBy(anotherUser);
        integration.setUsers(Collections.emptySet());

        when(userRepo.findById("user1")).thenReturn(Optional.of(user));
        when(integrationRepository.findById("int1")).thenReturn(Optional.of(integration));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            integrationService.getIntegrationById("int1", "user1", false);
        });
    }

    @Test
    void deleteIntegration_WhenUserIsCreator_ShouldDeleteIntegration() {
        // Arrange
        Integration integration = new Integration();
        integration.setId("int1");
        integration.setCreatedBy(user);
        integration.setUsers(Collections.emptySet());

        when(userRepo.findById("user1")).thenReturn(Optional.of(user));
        when(integrationRepository.findById("int1")).thenReturn(Optional.of(integration));
        doNothing().when(integrationRepository).delete(integration);

        // Act
        integrationService.deleteIntegration("int1", "user1");

        // Assert
        verify(integrationRepository, times(1)).delete(integration);
    }
} 