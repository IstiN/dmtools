package com.github.istin.dmtools.server.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception to indicate that a job configuration cannot be deleted
 * because it has active job executions.
 */
@ResponseStatus(HttpStatus.CONFLICT) // 409 Conflict
public class JobHasActiveExecutionsException extends RuntimeException {

    public JobHasActiveExecutionsException(String message) {
        super(message);
    }
}
