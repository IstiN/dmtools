package com.github.istin.dmtools.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CreateWebhookKeyRequestTest {

    @Test
    void testDefaultConstructor() {
        // Act
        CreateWebhookKeyRequest request = new CreateWebhookKeyRequest();

        // Assert
        assertNotNull(request);
        assertNull(request.getName());
        assertNull(request.getDescription());
    }

    @Test
    void testAllArgsConstructor() {
        // Act
        CreateWebhookKeyRequest request = new CreateWebhookKeyRequest("Test Key", "Test Description");

        // Assert
        assertEquals("Test Key", request.getName());
        assertEquals("Test Description", request.getDescription());
    }

    @Test
    void testGettersAndSetters() {
        // Arrange
        CreateWebhookKeyRequest request = new CreateWebhookKeyRequest();

        // Act
        request.setName("API Key Name");
        request.setDescription("API Key Description");

        // Assert
        assertEquals("API Key Name", request.getName());
        assertEquals("API Key Description", request.getDescription());
    }

    @Test
    void testWithNullValues() {
        // Arrange
        CreateWebhookKeyRequest request = new CreateWebhookKeyRequest();

        // Act
        request.setName(null);
        request.setDescription(null);

        // Assert
        assertNull(request.getName());
        assertNull(request.getDescription());
    }

    @Test
    void testWithEmptyValues() {
        // Arrange
        CreateWebhookKeyRequest request = new CreateWebhookKeyRequest();

        // Act
        request.setName("");
        request.setDescription("");

        // Assert
        assertEquals("", request.getName());
        assertEquals("", request.getDescription());
    }
}