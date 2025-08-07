package com.github.istin.dmtools.auth.service;

import com.github.istin.dmtools.auth.model.*;
import com.github.istin.dmtools.auth.repository.*;
import com.github.istin.dmtools.auth.util.EncryptionUtils;
import com.github.istin.dmtools.dto.CreateIntegrationRequest;
import com.github.istin.dmtools.dto.IntegrationDto;
import com.github.istin.dmtools.dto.IntegrationTypeDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    @Mock
    private IntegrationConfigurationLoader configurationLoader;

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
        when(integrationRepository.findById("int1")).thenAnswer(i -> {
            Integration integration = new Integration();
            integration.setId("int1");
            integration.setName("Test Integration");
            integration.setType("jira");
            integration.setCreatedBy(user);
            return Optional.of(integration);
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

    @Test
    void createGitHubIntegration_ShouldCreateWithAllParameters() {
        // Arrange
        CreateIntegrationRequest request = new CreateIntegrationRequest();
        request.setName("GitHub Integration");
        request.setDescription("GitHub integration for dmtools repository");
        request.setType("github");
        
        // Create config parameters map
        Map<String, CreateIntegrationRequest.ConfigParam> configParams = new LinkedHashMap<>();
        
        // Add GitHub-specific parameters (using JSON configuration parameter keys)
        configParams.put("SOURCE_GITHUB_TOKEN", 
            new CreateIntegrationRequest.ConfigParam("test_github_token_12345", true));
        configParams.put("SOURCE_GITHUB_WORKSPACE", 
            new CreateIntegrationRequest.ConfigParam("IstiN", false));
        configParams.put("SOURCE_GITHUB_REPOSITORY", 
            new CreateIntegrationRequest.ConfigParam("dmtools", false));
        configParams.put("SOURCE_GITHUB_BRANCH", 
            new CreateIntegrationRequest.ConfigParam("main", false));
        configParams.put("SOURCE_GITHUB_BASE_PATH", 
            new CreateIntegrationRequest.ConfigParam("https://api.github.com", false));
        
        request.setConfigParams(configParams);

        // Mock dependencies
        when(userRepo.findById("user1")).thenReturn(Optional.of(user));
        when(integrationRepository.save(any(Integration.class))).thenAnswer(i -> {
            Integration integration = i.getArgument(0);
            integration.setId("github_int1");
            return integration;
        });
        when(integrationRepository.findById("github_int1")).thenAnswer(i -> {
            Integration integration = new Integration();
            integration.setId("github_int1");
            integration.setName("GitHub Integration");
            integration.setDescription("GitHub integration for dmtools repository");
            integration.setType("github");
            integration.setCreatedBy(user);
            return Optional.of(integration);
        });
        when(encryptionUtils.encrypt("test_github_token_12345"))
            .thenReturn("encrypted_token_value");
        when(configRepository.save(any(IntegrationConfig.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        IntegrationDto result = integrationService.createIntegration(request, "user1");

        // Assert
        assertNotNull(result);
        assertEquals("GitHub Integration", result.getName());
        assertEquals("GitHub integration for dmtools repository", result.getDescription());
        assertEquals("github", result.getType());
        assertEquals("github_int1", result.getId());
        assertTrue(result.isEnabled());
        
        // Verify integration was saved
        verify(integrationRepository, times(1)).save(any(Integration.class));
        
        // Verify all config parameters were saved (5 parameters)
        verify(configRepository, times(5)).save(any(IntegrationConfig.class));
        
        // Verify that sensitive token was encrypted
        verify(encryptionUtils, times(1)).encrypt("test_github_token_12345");
        
        // Verify the correct integration was created with proper user assignment
        verify(integrationRepository).save(argThat(integration -> 
            integration.getName().equals("GitHub Integration") &&
            integration.getType().equals("github") &&
            integration.getCreatedBy().equals(user) &&
            integration.isEnabled()
        ));
        
        // Verify config parameters were saved correctly
        verify(configRepository).save(argThat(config -> 
            config.getParamKey().equals("SOURCE_GITHUB_TOKEN") &&
            config.isSensitive() &&
            config.getParamValue().equals("encrypted_token_value")
        ));
        
        verify(configRepository).save(argThat(config -> 
            config.getParamKey().equals("SOURCE_GITHUB_WORKSPACE") &&
            !config.isSensitive() &&
            config.getParamValue().equals("IstiN")
        ));
        
        verify(configRepository).save(argThat(config -> 
            config.getParamKey().equals("SOURCE_GITHUB_REPOSITORY") &&
            !config.isSensitive() &&
            config.getParamValue().equals("dmtools")
        ));
        
        verify(configRepository).save(argThat(config -> 
            config.getParamKey().equals("SOURCE_GITHUB_BRANCH") &&
            !config.isSensitive() &&
            config.getParamValue().equals("main")
        ));
        
        verify(configRepository).save(argThat(config -> 
            config.getParamKey().equals("SOURCE_GITHUB_BASE_PATH") &&
            !config.isSensitive() &&
            config.getParamValue().equals("https://api.github.com")
        ));
    }
    
    @Test
    void getAvailableIntegrationTypes_ShouldReturnJSONConfiguredTypes() {
        // Arrange
        IntegrationTypeDto github = createMockGitHubIntegrationTypeDto();
        IntegrationTypeDto jira = createMockJiraIntegrationTypeDto();
        IntegrationTypeDto dial = createMockDialIntegrationTypeDto();
        
        when(configurationLoader.getAllIntegrationTypes())
                .thenReturn(List.of(github, jira, dial));
        
        // When
        List<IntegrationTypeDto> result = integrationService.getAvailableIntegrationTypes();
        
        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        
        // Should contain GitHub integration type with categories
        IntegrationTypeDto actualGithub = result.stream()
                .filter(type -> "github".equals(type.getType()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("GitHub integration type not found"));
        
        assertEquals("GitHub", actualGithub.getDisplayName());
        assertEquals("Integration with GitHub for source code management", actualGithub.getDescription());
        assertTrue(actualGithub.getCategories().contains("SourceCode"));
        
        // Should have all expected configuration parameters
        assertNotNull(actualGithub.getConfigParams());
        assertTrue(actualGithub.getConfigParams().stream()
                .anyMatch(p -> "SOURCE_GITHUB_TOKEN".equals(p.getKey()) && p.isRequired() && p.isSensitive()));
        assertTrue(actualGithub.getConfigParams().stream()
                .anyMatch(p -> "SOURCE_GITHUB_BASE_PATH".equals(p.getKey()) && 
                              "https://api.github.com".equals(p.getDefaultValue())));
        
        // Should contain other integration types
        assertTrue(result.stream().anyMatch(type -> "jira".equals(type.getType())));
        assertTrue(result.stream().anyMatch(type -> "dial".equals(type.getType())));
        
        verify(configurationLoader, times(1)).getAllIntegrationTypes();
    }
    
    @Test
    void getIntegrationTypeSchema_GitHub_ShouldReturnJSONConfiguration() {
        // Arrange
        IntegrationTypeDto github = createMockGitHubIntegrationTypeDto();
        when(configurationLoader.getIntegrationType("github")).thenReturn(github);
        
        // When
        IntegrationTypeDto result = integrationService.getIntegrationTypeSchema("github");
        
        // Then
        assertNotNull(result);
        assertEquals("github", result.getType());
        assertEquals("GitHub", result.getDisplayName());
        assertTrue(result.getCategories().contains("SourceCode"));
        
        // Should have configuration parameters with instructions
        assertNotNull(result.getConfigParams());
        assertFalse(result.getConfigParams().isEmpty());
        
        // All parameters should have instructions and descriptions
        for (IntegrationTypeDto.ConfigParamDefinition param : result.getConfigParams()) {
            assertNotNull(param.getDescription());
            assertNotNull(param.getInstructions());
            assertFalse(param.getDescription().trim().isEmpty());
            assertFalse(param.getInstructions().trim().isEmpty());
        }
        
        verify(configurationLoader, times(1)).getIntegrationType("github");
    }
    
    // Helper methods for creating mock DTOs
    private IntegrationTypeDto createMockGitHubIntegrationTypeDto() {
        IntegrationTypeDto github = new IntegrationTypeDto();
        github.setType("github");
        github.setDisplayName("GitHub");
        github.setDescription("Integration with GitHub for source code management");
        github.setIconUrl("/img/integrations/github-icon.svg");
        github.setCategories(List.of("SourceCode"));
        
        List<IntegrationTypeDto.ConfigParamDefinition> params = new ArrayList<>();
        
        IntegrationTypeDto.ConfigParamDefinition tokenParam = new IntegrationTypeDto.ConfigParamDefinition();
        tokenParam.setKey("SOURCE_GITHUB_TOKEN");
        tokenParam.setDisplayName("GitHub Token");
        tokenParam.setDescription("Your GitHub personal access token");
        tokenParam.setInstructions("Generate a personal access token from GitHub Settings > Developer settings > Personal access tokens");
        tokenParam.setRequired(true);
        tokenParam.setSensitive(true);
        tokenParam.setType("password");
        
        IntegrationTypeDto.ConfigParamDefinition basePathParam = new IntegrationTypeDto.ConfigParamDefinition();
        basePathParam.setKey("SOURCE_GITHUB_BASE_PATH");
        basePathParam.setDisplayName("GitHub Base Path");
        basePathParam.setDescription("The GitHub API base path");
        basePathParam.setInstructions("Use default value unless using GitHub Enterprise");
        basePathParam.setRequired(false);
        basePathParam.setSensitive(false);
        basePathParam.setType("url");
        basePathParam.setDefaultValue("https://api.github.com");
        
        params.add(tokenParam);
        params.add(basePathParam);
        github.setConfigParams(params);
        
        return github;
    }
    
    private IntegrationTypeDto createMockJiraIntegrationTypeDto() {
        IntegrationTypeDto jira = new IntegrationTypeDto();
        jira.setType("jira");
        jira.setDisplayName("Jira");
        jira.setDescription("Integration with Atlassian Jira for issue tracking");
        jira.setIconUrl("/img/integrations/jira-icon.svg");
        jira.setCategories(List.of("TrackerClient"));
        jira.setConfigParams(new ArrayList<>());
        return jira;
    }
    
    private IntegrationTypeDto createMockDialIntegrationTypeDto() {
        IntegrationTypeDto dial = new IntegrationTypeDto();
        dial.setType("dial");
        dial.setDisplayName("Dial");
        dial.setDescription("Integration with Dial API for AI capabilities");
        dial.setIconUrl("/img/integrations/dial-icon.svg");
        dial.setCategories(List.of("AI"));
        dial.setConfigParams(new ArrayList<>());
        return dial;
    }
} 