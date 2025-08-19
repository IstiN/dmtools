package com.github.istin.dmtools.auth.service;

import com.github.istin.dmtools.auth.model.McpConfiguration;
import com.github.istin.dmtools.auth.model.User;
import com.github.istin.dmtools.auth.repository.McpConfigurationRepository;
import com.github.istin.dmtools.dto.CreateMcpConfigurationRequest;
import com.github.istin.dmtools.dto.IntegrationDto;
import com.github.istin.dmtools.dto.McpConfigurationDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class McpConfigurationServiceTest {

    @Mock
    private McpConfigurationRepository mcpConfigurationRepository;

    @Mock
    private IntegrationService integrationService;

    @Mock
    private UserService userService;

    @InjectMocks
    private McpConfigurationService mcpConfigurationService;

    private User testUser;
    private McpConfiguration testConfiguration;
    private CreateMcpConfigurationRequest testRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("user-123");
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");

        testConfiguration = new McpConfiguration();
        testConfiguration.setId("config-123");
        testConfiguration.setName("Test MCP");
        testConfiguration.setUser(testUser);
        testConfiguration.setIntegrationIds(List.of("integration-1", "integration-2"));
        testConfiguration.setCreatedAt(LocalDateTime.now());
        testConfiguration.setUpdatedAt(LocalDateTime.now());

        testRequest = new CreateMcpConfigurationRequest();
        testRequest.setName("Test MCP");
        testRequest.setIntegrationIds(List.of("integration-1", "integration-2"));
    }

    @Test
    void getUserConfigurations_ShouldReturnUserConfigurations() {
        // Given
        when(userService.findById("user-123")).thenReturn(Optional.of(testUser));
        when(mcpConfigurationRepository.findByUserOrderByCreatedAtDesc(testUser))
                .thenReturn(List.of(testConfiguration));

        // When
        List<McpConfigurationDto> result = mcpConfigurationService.getUserConfigurations("user-123");

        // Then
        assertEquals(1, result.size());
        assertEquals("config-123", result.get(0).getId());
        assertEquals("Test MCP", result.get(0).getName());
        assertEquals("user-123", result.get(0).getUserId());
    }

    @Test
    void getUserConfigurations_UserNotFound_ShouldThrowException() {
        // Given
        when(userService.findById("user-123")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, 
                () -> mcpConfigurationService.getUserConfigurations("user-123"));
    }

    @Test
    void createConfiguration_ValidRequest_ShouldCreateConfiguration() {
        // Given
        when(userService.findById("user-123")).thenReturn(Optional.of(testUser));
        when(mcpConfigurationRepository.existsByNameAndUser(testRequest.getName(), testUser))
                .thenReturn(false);
        when(mcpConfigurationRepository.save(any(McpConfiguration.class)))
                .thenReturn(testConfiguration);

        // Mock integration validation - create mock DTOs to return
        IntegrationDto mockIntegration1 = new IntegrationDto();
        mockIntegration1.setId("integration-1");
        mockIntegration1.setName("Test Integration 1");
        
        IntegrationDto mockIntegration2 = new IntegrationDto();
        mockIntegration2.setId("integration-2");
        mockIntegration2.setName("Test Integration 2");
        
        when(integrationService.getIntegrationById("integration-1", "user-123", false))
                .thenReturn(mockIntegration1);
        when(integrationService.getIntegrationById("integration-2", "user-123", false))
                .thenReturn(mockIntegration2);

        // When
        McpConfigurationDto result = mcpConfigurationService.createConfiguration(testRequest, "user-123");

        // Then
        assertEquals("config-123", result.getId());
        assertEquals("Test MCP", result.getName());
        verify(mcpConfigurationRepository).save(any(McpConfiguration.class));
    }

    @Test
    void createConfiguration_DuplicateName_ShouldThrowException() {
        // Given
        when(userService.findById("user-123")).thenReturn(Optional.of(testUser));
        when(mcpConfigurationRepository.existsByNameAndUser(testRequest.getName(), testUser))
                .thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, 
                () -> mcpConfigurationService.createConfiguration(testRequest, "user-123"));
    }

    @Test
    void createConfiguration_InvalidIntegration_ShouldThrowException() {
        // Given
        when(userService.findById("user-123")).thenReturn(Optional.of(testUser));
        when(mcpConfigurationRepository.existsByNameAndUser(testRequest.getName(), testUser))
                .thenReturn(false);
        
        // Mock integration validation failure
        doThrow(new IllegalArgumentException("Integration not found"))
                .when(integrationService).getIntegrationById("integration-1", "user-123", false);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> mcpConfigurationService.createConfiguration(testRequest, "user-123"));
        assertTrue(exception.getMessage().contains("Integration not accessible"));
    }

    @Test
    void updateConfiguration_ValidRequest_ShouldUpdateConfiguration() {
        // Given
        when(userService.findById("user-123")).thenReturn(Optional.of(testUser));
        when(mcpConfigurationRepository.findByIdAndUser("config-123", testUser))
                .thenReturn(Optional.of(testConfiguration));
        when(mcpConfigurationRepository.existsByNameAndUserAndIdNot(
                testRequest.getName(), testUser, "config-123"))
                .thenReturn(false);
        when(mcpConfigurationRepository.save(any(McpConfiguration.class)))
                .thenReturn(testConfiguration);

        // Mock integration validation - create mock DTOs to return
        IntegrationDto mockIntegration1 = new IntegrationDto();
        mockIntegration1.setId("integration-1");
        mockIntegration1.setName("Test Integration 1");
        
        IntegrationDto mockIntegration2 = new IntegrationDto();
        mockIntegration2.setId("integration-2");
        mockIntegration2.setName("Test Integration 2");
        
        when(integrationService.getIntegrationById("integration-1", "user-123", false))
                .thenReturn(mockIntegration1);
        when(integrationService.getIntegrationById("integration-2", "user-123", false))
                .thenReturn(mockIntegration2);

        // When
        McpConfigurationDto result = mcpConfigurationService.updateConfiguration(
                "config-123", testRequest, "user-123");

        // Then
        assertEquals("config-123", result.getId());
        verify(mcpConfigurationRepository).save(testConfiguration);
    }

    @Test
    void updateConfiguration_ConfigurationNotFound_ShouldThrowException() {
        // Given
        when(userService.findById("user-123")).thenReturn(Optional.of(testUser));
        when(mcpConfigurationRepository.findByIdAndUser("config-123", testUser))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, 
                () -> mcpConfigurationService.updateConfiguration(
                        "config-123", testRequest, "user-123"));
    }

    @Test
    void deleteConfiguration_ValidRequest_ShouldDeleteConfiguration() {
        // Given
        when(userService.findById("user-123")).thenReturn(Optional.of(testUser));
        when(mcpConfigurationRepository.findByIdAndUser("config-123", testUser))
                .thenReturn(Optional.of(testConfiguration));

        // When
        mcpConfigurationService.deleteConfiguration("config-123", "user-123");

        // Then
        verify(mcpConfigurationRepository).delete(testConfiguration);
    }

    @Test
    void deleteConfiguration_ConfigurationNotFound_ShouldThrowException() {
        // Given
        when(userService.findById("user-123")).thenReturn(Optional.of(testUser));
        when(mcpConfigurationRepository.findByIdAndUser("config-123", testUser))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, 
                () -> mcpConfigurationService.deleteConfiguration("config-123", "user-123"));
    }

    @Test
    void generateAccessCode_CursorFormat_ShouldGenerateValidCode() {
        // Given
        when(userService.findById("user-123")).thenReturn(Optional.of(testUser));
        when(mcpConfigurationRepository.findByIdAndUser("config-123", testUser))
                .thenReturn(Optional.of(testConfiguration));

        // When
        var result = mcpConfigurationService.generateAccessCode("config-123", "user-123", "cursor");

        // Then
        assertEquals("Test MCP", result.getConfigurationName());
        assertEquals("cursor", result.getFormat());
        assertTrue(result.getCode().contains("mcpServers"));
        assertTrue(result.getEndpointUrl().contains("/mcp/config/config-123"));
        assertNotNull(result.getInstructions());
    }

    @Test
    void generateAccessCode_JsonFormat_ShouldGenerateValidCode() {
        // Given
        when(userService.findById("user-123")).thenReturn(Optional.of(testUser));
        when(mcpConfigurationRepository.findByIdAndUser("config-123", testUser))
                .thenReturn(Optional.of(testConfiguration));

        // When
        var result = mcpConfigurationService.generateAccessCode("config-123", "user-123", "json");

        // Then
        assertEquals("json", result.getFormat());
        assertTrue(result.getCode().contains("\"name\": \"Test MCP\""));
        assertTrue(result.getCode().contains("\"protocol\": \"MCP\""));
    }

    @Test
    void generateAccessCode_ShellFormat_ShouldGenerateValidCode() {
        // Given
        when(userService.findById("user-123")).thenReturn(Optional.of(testUser));
        when(mcpConfigurationRepository.findByIdAndUser("config-123", testUser))
                .thenReturn(Optional.of(testConfiguration));

        // When
        var result = mcpConfigurationService.generateAccessCode("config-123", "user-123", "shell");

        // Then
        assertEquals("shell", result.getFormat());
        assertTrue(result.getCode().contains("#!/bin/bash"));
        assertTrue(result.getCode().contains("MCP_SERVER_NAME"));
    }

    @Test
    void generateAccessCode_GeminiFormat_ShouldGenerateValidCode() {
        // Given
        when(userService.findById("user-123")).thenReturn(Optional.of(testUser));
        when(mcpConfigurationRepository.findByIdAndUser("config-123", testUser))
                .thenReturn(Optional.of(testConfiguration));

        // When
        var result = mcpConfigurationService.generateAccessCode("config-123", "user-123", "gemini");

        // Then
        assertEquals("gemini", result.getFormat());
        assertTrue(result.getCode().contains("\"mcpServers\""));
        assertTrue(result.getCode().contains("\"httpUrl\""));
        assertTrue(result.getCode().contains("/mcp/stream/"));
        assertTrue(result.getInstructions().contains("~/.gemini/settings.json"));
    }

    @Test
    void generateAccessCode_InvalidFormat_ShouldThrowException() {
        // Given
        when(userService.findById("user-123")).thenReturn(Optional.of(testUser));
        when(mcpConfigurationRepository.findByIdAndUser("config-123", testUser))
                .thenReturn(Optional.of(testConfiguration));

        // When & Then
        assertThrows(IllegalArgumentException.class, 
                () -> mcpConfigurationService.generateAccessCode("config-123", "user-123", "invalid"));
    }
} 