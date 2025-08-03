package com.github.istin.dmtools.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class CreateWebhookKeyResponseTest {

    @Test
    void testDefaultConstructor() {
        // Act
        CreateWebhookKeyResponse response = new CreateWebhookKeyResponse();

        // Assert
        assertNotNull(response);
        assertNull(response.getKeyId());
        assertNull(response.getApiKey());
        assertNull(response.getName());
        assertNull(response.getDescription());
        assertNull(response.getJobConfigurationId());
        assertNull(response.getCreatedAt());
    }

    @Test
    void testAllArgsConstructor() {
        // Arrange
        LocalDateTime testTime = LocalDateTime.now();

        // Act
        CreateWebhookKeyResponse response = new CreateWebhookKeyResponse(
                "key-123", "wk_apikey123", "Test Key", "Test Description", "job-123", testTime);

        // Assert
        assertEquals("key-123", response.getKeyId());
        assertEquals("wk_apikey123", response.getApiKey());
        assertEquals("Test Key", response.getName());
        assertEquals("Test Description", response.getDescription());
        assertEquals("job-123", response.getJobConfigurationId());
        assertEquals(testTime, response.getCreatedAt());
    }

    @Test
    void testCreateMethod() {
        // Arrange
        LocalDateTime testTime = LocalDateTime.now();

        // Act
        CreateWebhookKeyResponse response = CreateWebhookKeyResponse.create(
                "key-456", "wk_generated123", "Generated Key", "Generated Description", "job-456", testTime);

        // Assert
        assertEquals("key-456", response.getKeyId());
        assertEquals("wk_generated123", response.getApiKey());
        assertEquals("Generated Key", response.getName());
        assertEquals("Generated Description", response.getDescription());
        assertEquals("job-456", response.getJobConfigurationId());
        assertEquals(testTime, response.getCreatedAt());
    }

    @Test
    void testGettersAndSetters() {
        // Arrange
        CreateWebhookKeyResponse response = new CreateWebhookKeyResponse();
        LocalDateTime testTime = LocalDateTime.now();

        // Act
        response.setKeyId("test-key");
        response.setApiKey("wk_testkey");
        response.setName("Test Name");
        response.setDescription("Test Desc");
        response.setJobConfigurationId("test-job");
        response.setCreatedAt(testTime);

        // Assert
        assertEquals("test-key", response.getKeyId());
        assertEquals("wk_testkey", response.getApiKey());
        assertEquals("Test Name", response.getName());
        assertEquals("Test Desc", response.getDescription());
        assertEquals("test-job", response.getJobConfigurationId());
        assertEquals(testTime, response.getCreatedAt());
    }

    @Test
    void testCreateMethod_WithNullValues() {
        // Act
        CreateWebhookKeyResponse response = CreateWebhookKeyResponse.create(
                "key-789", "wk_nulltest", null, null, "job-789", null);

        // Assert
        assertEquals("key-789", response.getKeyId());
        assertEquals("wk_nulltest", response.getApiKey());
        assertNull(response.getName());
        assertNull(response.getDescription());
        assertEquals("job-789", response.getJobConfigurationId());
        assertNull(response.getCreatedAt());
    }
}