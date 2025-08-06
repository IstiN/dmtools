package com.github.istin.dmtools.server.exception;

/**
 * Exception thrown when parameter validation fails.
 */
public class ValidationException extends Exception {
    
    public ValidationException(String message) {
        super(message);
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}