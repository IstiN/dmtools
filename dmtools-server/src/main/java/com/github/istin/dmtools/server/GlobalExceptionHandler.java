package com.github.istin.dmtools.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    protected Logger getLogger() {
        return LoggerFactory.getLogger(GlobalExceptionHandler.class);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", "An unexpected error occurred: " + ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleException(Exception ex) {
        getLogger().error("Runtime exception occurred", ex);
        Map<String, String> body = new HashMap<>();
        body.put("error", "An unexpected error occurred");
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        getLogger().error("Data integrity violation", ex);
        Map<String, String> body = new HashMap<>();
        body.put("error", "Data integrity violation");
        
        // Check for UUID conversion errors
        if (ex.getMessage() != null && ex.getMessage().contains("Data conversion error converting") && 
            ex.getMessage().contains("[22018-224]")) {
            body.put("message", "Database ID type mismatch. Please check entity ID types.");
            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        getLogger().error("Method argument type mismatch", ex);
        Map<String, String> body = new HashMap<>();
        body.put("error", "Invalid parameter type");
        
        // Check for specific "undefined" value
        if ("undefined".equals(String.valueOf(ex.getValue()))) {
            body.put("message", "Invalid ID: 'undefined' is not a valid identifier");
        } else {
            body.put("message", String.format("Failed to convert value '%s' to required type '%s' for parameter '%s'", 
                ex.getValue(), ex.getRequiredType().getSimpleName(), ex.getName()));
        }
        
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
} 