package com.github.istin.dmtools.server.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception for job configuration deletion failures.
 * This exception is typically thrown when a job configuration cannot be deleted
 * due to existing active job executions.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class JobConfigurationDeletionException extends RuntimeException {

    public JobConfigurationDeletionException(String message) {
        super(message);
    }

    public JobConfigurationDeletionException(String message, Throwable cause) {
        super(message, cause);
    }
}
