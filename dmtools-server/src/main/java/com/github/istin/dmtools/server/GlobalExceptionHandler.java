package com.github.istin.dmtools.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    // This method allows for logger injection in tests
    protected Logger getLogger() {
        return logger;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        getLogger().error("❌ An unexpected error occurred", e);
        
        Map<String, String> response = new HashMap<>();
        response.put("error", "An unexpected error occurred");
        response.put("message", e.getMessage());
        
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        getLogger().error("❌ Data integrity violation", e);
        
        Map<String, String> response = new HashMap<>();
        response.put("error", "Data integrity violation");
        
        // Check if it's the UUID conversion error
        if (e.getMessage() != null && e.getMessage().contains("Data conversion error converting")) {
            response.put("message", "Database ID type mismatch. Please check entity ID types.");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        response.put("message", e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        getLogger().error("❌ Invalid parameter type", e);
        
        Map<String, String> response = new HashMap<>();
        response.put("error", "Invalid parameter type");
        
        // Check if it's the "undefined" error
        if (e.getValue() != null && "undefined".equals(e.getValue().toString())) {
            response.put("message", "Invalid ID: 'undefined' is not a valid identifier");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        
        response.put("message", "Failed to convert value '" + e.getValue() + "' to required type '" + e.getRequiredType().getSimpleName() + "'");
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
} 