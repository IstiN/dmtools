package com.github.istin.dmtools.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PresentationResponseTest {

    @Test
    void testNoArgsConstructor() {
        PresentationResponse response = new PresentationResponse();
        assertNull(response.getPresentationJson());
        assertNull(response.getFilePath());
        assertNull(response.getMessage());
        assertFalse(response.isSuccess());
    }

    @Test
    void testAllArgsConstructor() {
        String presentationJson = "{\"title\":\"Test Presentation\",\"slides\":[{\"title\":\"Slide 1\",\"content\":\"Content\"}]}";
        String filePath = "/path/to/presentation.html";
        String message = "Test message";
        boolean success = true;

        PresentationResponse response = new PresentationResponse(presentationJson, filePath, message, success);
        
        assertEquals(presentationJson, response.getPresentationJson());
        assertEquals(filePath, response.getFilePath());
        assertEquals(message, response.getMessage());
        assertEquals(success, response.isSuccess());
    }

    @Test
    void testSettersAndGetters() {
        PresentationResponse response = new PresentationResponse();
        
        String presentationJson = "{\"title\":\"Test Presentation\",\"slides\":[{\"title\":\"Slide 1\",\"content\":\"Content\"}]}";
        String filePath = "/path/to/presentation.html";
        String message = "Test message";
        boolean success = true;
        
        response.setPresentationJson(presentationJson);
        response.setFilePath(filePath);
        response.setMessage(message);
        response.setSuccess(success);
        
        assertEquals(presentationJson, response.getPresentationJson());
        assertEquals(filePath, response.getFilePath());
        assertEquals(message, response.getMessage());
        assertEquals(success, response.isSuccess());
    }

    @Test
    void testEqualsAndHashCode() {
        String presentationJson = "{\"title\":\"Test Presentation\",\"slides\":[{\"title\":\"Slide 1\",\"content\":\"Content\"}]}";
        String filePath = "/path/to/presentation.html";
        
        PresentationResponse response1 = new PresentationResponse(presentationJson, filePath, "Success", true);
        PresentationResponse response2 = new PresentationResponse(presentationJson, filePath, "Success", true);
        PresentationResponse response3 = new PresentationResponse(presentationJson, "/different/path", "Success", true);
        
        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
        assertNotEquals(response1, response3);
        assertNotEquals(response1.hashCode(), response3.hashCode());
    }

    @Test
    void testStaticSuccess() {
        String presentationJson = "{\"title\":\"Test Presentation\",\"slides\":[{\"title\":\"Slide 1\",\"content\":\"Content\"}]}";
        String filePath = "/path/to/presentation.html";
        
        PresentationResponse response = PresentationResponse.success(presentationJson, filePath);
        
        assertEquals(presentationJson, response.getPresentationJson());
        assertEquals(filePath, response.getFilePath());
        assertEquals("Presentation created successfully", response.getMessage());
        assertTrue(response.isSuccess());
    }

    @Test
    void testStaticError() {
        String errorMessage = "Error creating presentation";
        
        PresentationResponse response = PresentationResponse.error(errorMessage);
        
        assertNull(response.getPresentationJson());
        assertNull(response.getFilePath());
        assertEquals(errorMessage, response.getMessage());
        assertFalse(response.isSuccess());
    }

    @Test
    void testToString() {
        String presentationJson = "{\"title\":\"Test\"}";
        String filePath = "/path/to/file.html";
        String message = "Success message";
        
        PresentationResponse response = new PresentationResponse(presentationJson, filePath, message, true);
        String toString = response.toString();
        
        assertTrue(toString.contains("presentationJson=" + presentationJson));
        assertTrue(toString.contains("filePath=" + filePath));
        assertTrue(toString.contains("message=" + message));
        assertTrue(toString.contains("success=true"));
    }
} 