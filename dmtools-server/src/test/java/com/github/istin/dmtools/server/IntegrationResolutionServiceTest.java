package com.github.istin.dmtools.server;

import com.github.istin.dmtools.common.utils.PropertyReader;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IntegrationResolutionServiceTest {

    @Mock
    private PropertyReader propertyReader;

    private IntegrationResolutionService integrationResolutionService;

    @BeforeEach
    void setUp() {
        integrationResolutionService = new IntegrationResolutionService();
        ReflectionTestUtils.setField(integrationResolutionService, "propertyReader", propertyReader);
    }

    @Test
    void testResolveOllamaIntegration_withAllParameters() {
        // Arrange
        when(propertyReader.getOllamaBasePath()).thenReturn("http://localhost:11434");
        when(propertyReader.getOllamaModel()).thenReturn("llama3");
        when(propertyReader.getOllamaNumCtx()).thenReturn(16384);
        when(propertyReader.getOllamaNumPredict()).thenReturn(-1);

        // Act
        JSONObject result = integrationResolutionService.resolveIntegrationsForJob("test-job", Arrays.asList("ollama"));

        // Assert
        assertNotNull(result);
        assertTrue(result.has("ollama"));
        
        JSONObject ollamaConfig = result.getJSONObject("ollama");
        assertEquals("http://localhost:11434", ollamaConfig.getString("OLLAMA_BASE_PATH"));
        assertEquals("llama3", ollamaConfig.getString("OLLAMA_MODEL"));
        assertEquals(16384, ollamaConfig.getInt("OLLAMA_NUM_CTX"));
        assertEquals(-1, ollamaConfig.getInt("OLLAMA_NUM_PREDICT"));
        
        verify(propertyReader).getOllamaBasePath();
        verify(propertyReader).getOllamaModel();
        verify(propertyReader).getOllamaNumCtx();
        verify(propertyReader).getOllamaNumPredict();
    }

    @Test
    void testResolveOllamaIntegration_withDefaultValues() {
        // Arrange
        when(propertyReader.getOllamaBasePath()).thenReturn(null);
        when(propertyReader.getOllamaModel()).thenReturn("mistral");
        when(propertyReader.getOllamaNumCtx()).thenReturn(8192);
        when(propertyReader.getOllamaNumPredict()).thenReturn(2048);

        // Act
        JSONObject result = integrationResolutionService.resolveIntegrationsForJob("test-job", Arrays.asList("ollama"));

        // Assert
        assertNotNull(result);
        assertTrue(result.has("ollama"));
        
        JSONObject ollamaConfig = result.getJSONObject("ollama");
        assertFalse(ollamaConfig.has("OLLAMA_BASE_PATH")); // Should not include null/empty values
        assertEquals("mistral", ollamaConfig.getString("OLLAMA_MODEL"));
        assertEquals(8192, ollamaConfig.getInt("OLLAMA_NUM_CTX"));
        assertEquals(2048, ollamaConfig.getInt("OLLAMA_NUM_PREDICT"));
    }

    @Test
    void testResolveOllamaIntegration_withEmptyBasePath() {
        // Arrange
        when(propertyReader.getOllamaBasePath()).thenReturn("   ");
        when(propertyReader.getOllamaModel()).thenReturn("codellama");
        when(propertyReader.getOllamaNumCtx()).thenReturn(32768);
        when(propertyReader.getOllamaNumPredict()).thenReturn(4096);

        // Act
        JSONObject result = integrationResolutionService.resolveIntegrationsForJob("test-job", Arrays.asList("ollama"));

        // Assert
        assertNotNull(result);
        assertTrue(result.has("ollama"));
        
        JSONObject ollamaConfig = result.getJSONObject("ollama");
        assertFalse(ollamaConfig.has("OLLAMA_BASE_PATH")); // Should not include whitespace-only values
        assertEquals("codellama", ollamaConfig.getString("OLLAMA_MODEL"));
    }

    @Test
    void testResolveOllamaIntegration_withEmptyModel() {
        // Arrange
        when(propertyReader.getOllamaBasePath()).thenReturn("http://localhost:11434");
        when(propertyReader.getOllamaModel()).thenReturn("");
        when(propertyReader.getOllamaNumCtx()).thenReturn(16384);
        when(propertyReader.getOllamaNumPredict()).thenReturn(-1);

        // Act
        JSONObject result = integrationResolutionService.resolveIntegrationsForJob("test-job", Arrays.asList("ollama"));

        // Assert
        assertNotNull(result);
        assertTrue(result.has("ollama"));
        
        JSONObject ollamaConfig = result.getJSONObject("ollama");
        assertEquals("http://localhost:11434", ollamaConfig.getString("OLLAMA_BASE_PATH"));
        assertFalse(ollamaConfig.has("OLLAMA_MODEL")); // Should not include empty model
        assertEquals(16384, ollamaConfig.getInt("OLLAMA_NUM_CTX"));
        assertEquals(-1, ollamaConfig.getInt("OLLAMA_NUM_PREDICT"));
    }

    @Test
    void testResolveOllamaIntegration_withCustomPort() {
        // Arrange
        when(propertyReader.getOllamaBasePath()).thenReturn("http://192.168.1.100:8080");
        when(propertyReader.getOllamaModel()).thenReturn("llama3.1");
        when(propertyReader.getOllamaNumCtx()).thenReturn(16384);
        when(propertyReader.getOllamaNumPredict()).thenReturn(-1);

        // Act
        JSONObject result = integrationResolutionService.resolveIntegrationsForJob("test-job", Arrays.asList("ollama"));

        // Assert
        assertNotNull(result);
        assertTrue(result.has("ollama"));
        
        JSONObject ollamaConfig = result.getJSONObject("ollama");
        assertEquals("http://192.168.1.100:8080", ollamaConfig.getString("OLLAMA_BASE_PATH"));
        assertEquals("llama3.1", ollamaConfig.getString("OLLAMA_MODEL"));
    }

    @Test
    void testResolveMultipleIntegrations_includingOllama() {
        // Arrange
        when(propertyReader.getOllamaBasePath()).thenReturn("http://localhost:11434");
        when(propertyReader.getOllamaModel()).thenReturn("llama3");
        when(propertyReader.getOllamaNumCtx()).thenReturn(16384);
        when(propertyReader.getOllamaNumPredict()).thenReturn(-1);
        
        when(propertyReader.getJiraBasePath()).thenReturn("https://test.atlassian.net");
        when(propertyReader.getJiraLoginPassToken()).thenReturn("test-token");
        when(propertyReader.getJiraAuthType()).thenReturn("Basic");

        // Act
        List<String> integrations = Arrays.asList("ollama", "jira");
        JSONObject result = integrationResolutionService.resolveIntegrationsForJob("test-job", integrations);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.length());
        assertTrue(result.has("ollama"));
        assertTrue(result.has("jira"));
        
        JSONObject ollamaConfig = result.getJSONObject("ollama");
        assertEquals("llama3", ollamaConfig.getString("OLLAMA_MODEL"));
        
        JSONObject jiraConfig = result.getJSONObject("jira");
        assertEquals("https://test.atlassian.net", jiraConfig.getString("url"));
    }

    @Test
    void testResolveDialIntegration_existingFunctionality() {
        // Arrange
        when(propertyReader.getDialIApiKey()).thenReturn("test-api-key");
        when(propertyReader.getDialModel()).thenReturn("gpt-4");
        when(propertyReader.getDialBathPath()).thenReturn("https://api.openai.com/v1");

        // Act
        JSONObject result = integrationResolutionService.resolveIntegrationsForJob("test-job", Arrays.asList("dial"));

        // Assert
        assertNotNull(result);
        assertTrue(result.has("dial"));
        
        JSONObject dialConfig = result.getJSONObject("dial");
        assertEquals("test-api-key", dialConfig.getString("apiKey"));
        assertEquals("gpt-4", dialConfig.getString("model"));
        assertEquals("https://api.openai.com/v1", dialConfig.getString("basePath"));
    }

    @Test
    void testResolveIntegrationsForJob_emptyList() {
        // Act
        JSONObject result = integrationResolutionService.resolveIntegrationsForJob("test-job", Arrays.asList());

        // Assert
        assertNotNull(result);
        assertEquals(0, result.length());
        verifyNoInteractions(propertyReader);
    }

    @Test
    void testResolveIntegrationsForJob_unknownIntegrationType() {
        // Act
        JSONObject result = integrationResolutionService.resolveIntegrationsForJob("test-job", Arrays.asList("unknown-type"));

        // Assert
        assertNotNull(result);
        // Unknown integration types return empty config and are not added to result
        assertEquals(0, result.length());
    }

    @Test
    void testGetRequiredIntegrationsForJob_expertJob() {
        // Act
        List<String> required = integrationResolutionService.getRequiredIntegrationsForJob("expert");

        // Assert
        assertNotNull(required);
        assertEquals(3, required.size());
        assertTrue(required.contains("tracker"));
        assertTrue(required.contains("wiki"));
        assertTrue(required.contains("ai"));
    }

    @Test
    void testGetRequiredIntegrationsForJob_defaultJob() {
        // Act
        List<String> required = integrationResolutionService.getRequiredIntegrationsForJob("unknown-job");

        // Assert
        assertNotNull(required);
        assertEquals(2, required.size());
        assertTrue(required.contains("tracker"));
        assertTrue(required.contains("ai"));
    }
}
