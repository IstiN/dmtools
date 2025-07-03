package com.github.istin.dmtools.auth.service;

import com.github.istin.dmtools.dto.IntegrationTypeDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for IntegrationConfigurationLoader.
 */
@SpringBootTest(classes = com.github.istin.dmtools.server.DmToolsServerApplication.class)
public class IntegrationConfigurationLoaderTest {

    @Autowired
    private IntegrationConfigurationLoader configurationLoader;

    @Test
    public void testGetAllIntegrationTypes_ShouldReturnAllConfiguredTypes() {
        // When
        List<IntegrationTypeDto> integrationTypes = configurationLoader.getAllIntegrationTypes();

        // Then
        assertNotNull(integrationTypes);
        assertFalse(integrationTypes.isEmpty());
        
        // Should contain all the main integration types
        assertTrue(integrationTypes.stream().anyMatch(t -> "github".equals(t.getType())));
        assertTrue(integrationTypes.stream().anyMatch(t -> "jira".equals(t.getType())));
        assertTrue(integrationTypes.stream().anyMatch(t -> "openai".equals(t.getType())));
        assertTrue(integrationTypes.stream().anyMatch(t -> "gitlab".equals(t.getType())));
        assertTrue(integrationTypes.stream().anyMatch(t -> "bitbucket".equals(t.getType())));
        assertTrue(integrationTypes.stream().anyMatch(t -> "gemini".equals(t.getType())));
        assertTrue(integrationTypes.stream().anyMatch(t -> "confluence".equals(t.getType())));
        assertTrue(integrationTypes.stream().anyMatch(t -> "figma".equals(t.getType())));
        assertTrue(integrationTypes.stream().anyMatch(t -> "rally".equals(t.getType())));
        assertTrue(integrationTypes.stream().anyMatch(t -> "appcenter".equals(t.getType())));
        assertTrue(integrationTypes.stream().anyMatch(t -> "firebase".equals(t.getType())));
    }

    @Test
    public void testGetIntegrationType_GitHub_ShouldReturnCorrectConfiguration() {
        // When
        IntegrationTypeDto github = configurationLoader.getIntegrationType("github");

        // Then
        assertNotNull(github);
        assertEquals("github", github.getType());
        assertEquals("GitHub", github.getDisplayName());
        assertEquals("Integration with GitHub for source code management", github.getDescription());
        assertEquals("/img/integrations/github-icon.svg", github.getIconUrl());
        
        // Should have SourceCode category
        assertTrue(github.getCategories().contains("SourceCode"));
        
        // Should have expected configuration parameters
        assertNotNull(github.getConfigParams());
        assertFalse(github.getConfigParams().isEmpty());
        
        // Check for required GitHub token parameter
        assertTrue(github.getConfigParams().stream()
                .anyMatch(p -> "SOURCE_GITHUB_TOKEN".equals(p.getKey()) && 
                              p.isRequired() && 
                              p.isSensitive() && 
                              "password".equals(p.getType())));
        
        // Check for base path parameter with default value
        assertTrue(github.getConfigParams().stream()
                .anyMatch(p -> "SOURCE_GITHUB_BASE_PATH".equals(p.getKey()) && 
                              "https://api.github.com".equals(p.getDefaultValue())));
    }

    @Test
    public void testGetIntegrationType_Jira_ShouldReturnCorrectConfiguration() {
        // When
        IntegrationTypeDto jira = configurationLoader.getIntegrationType("jira");

        // Then
        assertNotNull(jira);
        assertEquals("jira", jira.getType());
        assertEquals("Jira", jira.getDisplayName());
        assertTrue(jira.getCategories().contains("TrackerClient"));
        
        // Should have required parameters
        assertTrue(jira.getConfigParams().stream()
                .anyMatch(p -> "JIRA_BASE_PATH".equals(p.getKey()) && p.isRequired()));
        assertTrue(jira.getConfigParams().stream()
                .anyMatch(p -> "JIRA_LOGIN_PASS_TOKEN".equals(p.getKey()) && 
                              p.isRequired() && p.isSensitive()));
    }

    @Test
    public void testGetIntegrationType_OpenAI_ShouldReturnCorrectConfiguration() {
        // When
        IntegrationTypeDto openai = configurationLoader.getIntegrationType("openai");

        // Then
        assertNotNull(openai);
        assertEquals("openai", openai.getType());
        assertEquals("OpenAI", openai.getDisplayName());
        assertTrue(openai.getCategories().contains("AI"));
        
        // Should have API key parameter
        assertTrue(openai.getConfigParams().stream()
                .anyMatch(p -> "OPEN_AI_API_KEY".equals(p.getKey()) && 
                              p.isRequired() && p.isSensitive()));
        
        // Should have default model parameter
        assertTrue(openai.getConfigParams().stream()
                .anyMatch(p -> "OPEN_AI_MODEL".equals(p.getKey()) && 
                              "gpt-4".equals(p.getDefaultValue())));
    }

    @Test
    public void testGetIntegrationType_Gemini_ShouldReturnCorrectConfiguration() {
        // When
        IntegrationTypeDto gemini = configurationLoader.getIntegrationType("gemini");

        // Then
        assertNotNull(gemini);
        assertEquals("gemini", gemini.getType());
        assertEquals("Gemini", gemini.getDisplayName());
        assertTrue(gemini.getCategories().contains("AI"));
        
        // Should have API key parameter
        assertTrue(gemini.getConfigParams().stream()
                .anyMatch(p -> "GEMINI_API_KEY".equals(p.getKey()) && 
                              p.isRequired() && p.isSensitive()));
        
        // Should have default model parameter
        assertTrue(gemini.getConfigParams().stream()
                .anyMatch(p -> "GEMINI_DEFAULT_MODEL".equals(p.getKey()) && 
                              "gemini-2.0-flash".equals(p.getDefaultValue())));
    }

    @Test
    public void testGetIntegrationType_Firebase_ShouldReturnCorrectConfiguration() {
        // When
        IntegrationTypeDto firebase = configurationLoader.getIntegrationType("firebase");

        // Then
        assertNotNull(firebase);
        assertEquals("firebase", firebase.getType());
        assertEquals("Firebase", firebase.getDisplayName());
        assertTrue(firebase.getCategories().contains("Database"));
        assertTrue(firebase.getCategories().contains("Cloud"));
        
        // Should have project ID parameter
        assertTrue(firebase.getConfigParams().stream()
                .anyMatch(p -> "FIREBASE_PROJECT_ID".equals(p.getKey()) && 
                              p.isRequired() && !p.isSensitive()));
        
        // Should have service account JSON parameter as textarea
        assertTrue(firebase.getConfigParams().stream()
                .anyMatch(p -> "FIREBASE_SERVICE_ACCOUNT_JSON_AUTH".equals(p.getKey()) && 
                              p.isRequired() && p.isSensitive() && 
                              "textarea".equals(p.getType())));
    }

    @Test
    public void testGetIntegrationType_NonExistent_ShouldThrowException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> configurationLoader.getIntegrationType("nonexistent"));
        
        assertEquals("Integration type not found: nonexistent", exception.getMessage());
    }

    @Test
    public void testHasIntegrationType_ExistingType_ShouldReturnTrue() {
        // When & Then
        assertTrue(configurationLoader.hasIntegrationType("github"));
        assertTrue(configurationLoader.hasIntegrationType("jira"));
        assertTrue(configurationLoader.hasIntegrationType("openai"));
    }

    @Test
    public void testHasIntegrationType_NonExistentType_ShouldReturnFalse() {
        // When & Then
        assertFalse(configurationLoader.hasIntegrationType("nonexistent"));
        assertFalse(configurationLoader.hasIntegrationType("invalid"));
    }

    @Test
    public void testGetAllCategories_ShouldReturnAllUniqueCategories() {
        // When
        Set<String> categories = configurationLoader.getAllCategories();

        // Then
        assertNotNull(categories);
        assertFalse(categories.isEmpty());
        
        // Should contain expected categories
        assertTrue(categories.contains("SourceCode"));
        assertTrue(categories.contains("TrackerClient"));
        assertTrue(categories.contains("AI"));
        assertTrue(categories.contains("Documentation"));
        assertTrue(categories.contains("Design"));
        assertTrue(categories.contains("DevOps"));
        assertTrue(categories.contains("Database"));
        assertTrue(categories.contains("Cloud"));
    }

    @Test
    public void testGetIntegrationsByCategory_SourceCode_ShouldReturnSourceCodeIntegrations() {
        // When
        List<IntegrationTypeDto> sourceCodeIntegrations = 
                configurationLoader.getIntegrationsByCategory("SourceCode");

        // Then
        assertNotNull(sourceCodeIntegrations);
        assertFalse(sourceCodeIntegrations.isEmpty());
        
        // Should contain GitHub, GitLab, Bitbucket
        assertTrue(sourceCodeIntegrations.stream()
                .anyMatch(t -> "github".equals(t.getType())));
        assertTrue(sourceCodeIntegrations.stream()
                .anyMatch(t -> "gitlab".equals(t.getType())));
        assertTrue(sourceCodeIntegrations.stream()
                .anyMatch(t -> "bitbucket".equals(t.getType())));
        
        // Should not contain non-SourceCode integrations
        assertFalse(sourceCodeIntegrations.stream()
                .anyMatch(t -> "openai".equals(t.getType())));
    }

    @Test
    public void testGetIntegrationsByCategory_AI_ShouldReturnAIIntegrations() {
        // When
        List<IntegrationTypeDto> aiIntegrations = 
                configurationLoader.getIntegrationsByCategory("AI");

        // Then
        assertNotNull(aiIntegrations);
        assertFalse(aiIntegrations.isEmpty());
        
        // Should contain OpenAI and Gemini
        assertTrue(aiIntegrations.stream()
                .anyMatch(t -> "openai".equals(t.getType())));
        assertTrue(aiIntegrations.stream()
                .anyMatch(t -> "gemini".equals(t.getType())));
        
        // Should not contain non-AI integrations
        assertFalse(aiIntegrations.stream()
                .anyMatch(t -> "github".equals(t.getType())));
    }

    @Test
    public void testGetIntegrationsByCategory_TrackerClient_ShouldReturnTrackerIntegrations() {
        // When
        List<IntegrationTypeDto> trackerIntegrations = 
                configurationLoader.getIntegrationsByCategory("TrackerClient");

        // Then
        assertNotNull(trackerIntegrations);
        assertFalse(trackerIntegrations.isEmpty());
        
        // Should contain Jira and Rally
        assertTrue(trackerIntegrations.stream()
                .anyMatch(t -> "jira".equals(t.getType())));
        assertTrue(trackerIntegrations.stream()
                .anyMatch(t -> "rally".equals(t.getType())));
    }

    @Test
    public void testGetIntegrationsByCategory_NonExistentCategory_ShouldReturnEmptyList() {
        // When
        List<IntegrationTypeDto> integrations = 
                configurationLoader.getIntegrationsByCategory("NonExistentCategory");

        // Then
        assertNotNull(integrations);
        assertTrue(integrations.isEmpty());
    }

    @Test
    public void testConfigurationParameters_ShouldHaveInstructionsAndDescriptions() {
        // When
        IntegrationTypeDto github = configurationLoader.getIntegrationType("github");

        // Then
        assertNotNull(github.getConfigParams());
        
        // All parameters should have descriptions and instructions
        for (IntegrationTypeDto.ConfigParamDefinition param : github.getConfigParams()) {
            assertNotNull(param.getDescription(), 
                    "Parameter " + param.getKey() + " should have a description");
            assertFalse(param.getDescription().trim().isEmpty(), 
                    "Parameter " + param.getKey() + " description should not be empty");
            
            assertNotNull(param.getInstructions(), 
                    "Parameter " + param.getKey() + " should have instructions");
            assertFalse(param.getInstructions().trim().isEmpty(), 
                    "Parameter " + param.getKey() + " instructions should not be empty");
        }
    }

    @Test
    public void testConfigurationValidation_ShouldIncludeValidationRules() {
        // When
        IntegrationTypeDto github = configurationLoader.getIntegrationType("github");

        // Then
        // Find the GitHub token parameter
        IntegrationTypeDto.ConfigParamDefinition tokenParam = github.getConfigParams().stream()
                .filter(p -> "SOURCE_GITHUB_TOKEN".equals(p.getKey()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("GitHub token parameter not found"));

        // Should be marked as sensitive and required
        assertTrue(tokenParam.isSensitive());
        assertTrue(tokenParam.isRequired());
        assertEquals("password", tokenParam.getType());
    }
} 