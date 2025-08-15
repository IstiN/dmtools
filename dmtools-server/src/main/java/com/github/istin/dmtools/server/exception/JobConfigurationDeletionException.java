package com.github.istin.dmtools.server.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception to indicate that a job configuration cannot be deleted
 * due to existing associated job executions.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class JobConfigurationDeletionException extends RuntimeException {

    public JobConfigurationDeletionException(String message) {
        super(message);
    }

    public JobConfigurationDeletionException(String message, Throwable cause) {
        super(message, cause);
    }
}