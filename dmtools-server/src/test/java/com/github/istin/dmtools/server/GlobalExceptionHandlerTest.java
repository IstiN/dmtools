package com.github.istin.dmtools.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private Logger mockLogger;
    
    @InjectMocks
    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        // Create a new handler with a mocked logger
        handler = new TestGlobalExceptionHandler(mockLogger);
    }

    @Test
    void handleException_ReturnsInternalServerError() {
        Exception ex = new RuntimeException("Test exception");
        
        ResponseEntity<Map<String, String>> response = handler.handleException(ex);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred", response.getBody().get("error"));
        assertEquals("Test exception", response.getBody().get("message"));
    }

    @Test
    void handleDataIntegrityViolationException_UuidConversionError_ReturnsInternalServerError() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
                "Could not extract column [1] from JDBC ResultSet [Data conversion error converting \"240e22ca-e410-4807-ae66-c48bb4ebeb34\" [22018-224]] [n/a]");
        
        ResponseEntity<Map<String, String>> response = handler.handleDataIntegrityViolationException(ex);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Data integrity violation", response.getBody().get("error"));
        assertEquals("Database ID type mismatch. Please check entity ID types.", response.getBody().get("message"));
    }

    @Test
    void handleDataIntegrityViolationException_OtherError_ReturnsBadRequest() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("Some other data integrity error");
        
        ResponseEntity<Map<String, String>> response = handler.handleDataIntegrityViolationException(ex);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Data integrity violation", response.getBody().get("error"));
        assertEquals("Some other data integrity error", response.getBody().get("message"));
    }

    @Test
    void handleMethodArgumentTypeMismatchException_UndefinedError_ReturnsBadRequest() {
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "undefined", Long.class, "workspaceId", null, new NumberFormatException("For input string: \"undefined\""));
        
        ResponseEntity<Map<String, String>> response = handler.handleMethodArgumentTypeMismatchException(ex);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid parameter type", response.getBody().get("error"));
        assertEquals("Invalid ID: 'undefined' is not a valid identifier", response.getBody().get("message"));
    }

    @Test
    void handleMethodArgumentTypeMismatchException_OtherError_ReturnsBadRequest() {
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "abc", Long.class, "workspaceId", null, new NumberFormatException("For input string: \"abc\""));
        
        ResponseEntity<Map<String, String>> response = handler.handleMethodArgumentTypeMismatchException(ex);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid parameter type", response.getBody().get("error"));
        assertTrue(response.getBody().get("message").contains("Failed to convert value 'abc' to required type 'Long'"));
    }
    
    // Test subclass that allows us to inject a mocked logger
    private static class TestGlobalExceptionHandler extends GlobalExceptionHandler {
        private final Logger logger;
        
        public TestGlobalExceptionHandler(Logger logger) {
            this.logger = logger;
        }
        
        @Override
        protected Logger getLogger() {
            return logger;
        }
    }
} 