package com.github.istin.dmtools.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WebhookExecutionResponseTest {

    @Test
    void testDefaultConstructor() {
        // Act
        WebhookExecutionResponse response = new WebhookExecutionResponse();

        // Assert
        assertNotNull(response);
        assertNull(response.getExecutionId());
        assertNull(response.getStatus());
        assertNull(response.getMessage());
        assertNull(response.getJobConfigurationId());
    }

    @Test
    void testAllArgsConstructor() {
        // Act
        WebhookExecutionResponse response = new WebhookExecutionResponse(
                "exec-123", "PENDING", "Job started", "job-123");

        // Assert
        assertEquals("exec-123", response.getExecutionId());
        assertEquals("PENDING", response.getStatus());
        assertEquals("Job started", response.getMessage());
        assertEquals("job-123", response.getJobConfigurationId());
    }

    @Test
    void testSuccess() {
        // Act
        WebhookExecutionResponse response = WebhookExecutionResponse.success("exec-123", "job-123");

        // Assert
        assertEquals("exec-123", response.getExecutionId());
        assertEquals("PENDING", response.getStatus());
        assertEquals("Job execution started successfully", response.getMessage());
        assertEquals("job-123", response.getJobConfigurationId());
    }

    @Test
    void testError() {
        // Act
        WebhookExecutionResponse response = WebhookExecutionResponse.error("Invalid request", "INVALID_REQUEST");

        // Assert
        assertNull(response.getExecutionId());
        assertEquals("ERROR", response.getStatus());
        assertEquals("Invalid request", response.getMessage());
        assertNull(response.getJobConfigurationId());
    }

    @Test
    void testGettersAndSetters() {
        // Arrange
        WebhookExecutionResponse response = new WebhookExecutionResponse();

        // Act
        response.setExecutionId("exec-456");
        response.setStatus("RUNNING");
        response.setMessage("Job is running");
        response.setJobConfigurationId("job-456");

        // Assert
        assertEquals("exec-456", response.getExecutionId());
        assertEquals("RUNNING", response.getStatus());
        assertEquals("Job is running", response.getMessage());
        assertEquals("job-456", response.getJobConfigurationId());
    }
}