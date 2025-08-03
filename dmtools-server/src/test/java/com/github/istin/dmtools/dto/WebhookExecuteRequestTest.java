package com.github.istin.dmtools.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WebhookExecuteRequestTest {

    private ObjectMapper objectMapper;
    private WebhookExecuteRequest request;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        request = new WebhookExecuteRequest();
    }

    @Test
    void testToExecuteJobConfigurationRequest_WithParameters() {
        // Arrange
        JsonNode jobParams = objectMapper.createObjectNode()
                .put("inputJql", "key = TEST-123")
                .put("request", "Analyze this ticket");
        
        JsonNode integrationMappings = objectMapper.createObjectNode()
                .put("TrackerClient", "jira-integration-id")
                .put("AI", "openai-integration-id");

        request.setJobParameters(jobParams);
        request.setIntegrationMappings(integrationMappings);

        // Act
        ExecuteJobConfigurationRequest result = request.toExecuteJobConfigurationRequest();

        // Assert
        assertNotNull(result);
        assertEquals(jobParams, result.getParameterOverrides());
        assertEquals(integrationMappings, result.getIntegrationOverrides());
        assertEquals("SERVER_MANAGED", result.getExecutionMode());
    }

    @Test
    void testToExecuteJobConfigurationRequest_WithNullParameters() {
        // Arrange
        request.setJobParameters(null);
        request.setIntegrationMappings(null);

        // Act
        ExecuteJobConfigurationRequest result = request.toExecuteJobConfigurationRequest();

        // Assert
        assertNotNull(result);
        assertNull(result.getParameterOverrides());
        assertNull(result.getIntegrationOverrides());
        assertEquals("SERVER_MANAGED", result.getExecutionMode());
    }

    @Test
    void testToExecuteJobConfigurationRequest_EmptyRequest() {
        // Arrange
        // request is already empty from setUp

        // Act
        ExecuteJobConfigurationRequest result = request.toExecuteJobConfigurationRequest();

        // Assert
        assertNotNull(result);
        assertNull(result.getParameterOverrides());
        assertNull(result.getIntegrationOverrides());
        assertEquals("SERVER_MANAGED", result.getExecutionMode());
    }

    @Test
    void testGettersAndSetters() {
        // Arrange
        JsonNode jobParams = objectMapper.createObjectNode().put("test", "value");
        JsonNode integrationMappings = objectMapper.createObjectNode().put("integration", "mapping");

        // Act
        request.setJobParameters(jobParams);
        request.setIntegrationMappings(integrationMappings);

        // Assert
        assertEquals(jobParams, request.getJobParameters());
        assertEquals(integrationMappings, request.getIntegrationMappings());
    }

    @Test
    void testDefaultConstructor() {
        // Act
        WebhookExecuteRequest newRequest = new WebhookExecuteRequest();

        // Assert
        assertNotNull(newRequest);
        assertNull(newRequest.getJobParameters());
        assertNull(newRequest.getIntegrationMappings());
    }

    @Test
    void testAllArgsConstructor() {
        // Arrange
        JsonNode jobParams = objectMapper.createObjectNode().put("test", "value");
        JsonNode integrationMappings = objectMapper.createObjectNode().put("integration", "mapping");

        // Act
        WebhookExecuteRequest newRequest = new WebhookExecuteRequest(jobParams, integrationMappings);

        // Assert
        assertNotNull(newRequest);
        assertEquals(jobParams, newRequest.getJobParameters());
        assertEquals(integrationMappings, newRequest.getIntegrationMappings());
    }
}