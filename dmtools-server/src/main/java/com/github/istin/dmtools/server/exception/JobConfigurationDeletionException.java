package com.github.istin.dmtools.server.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT) // Or HttpStatus.BAD_REQUEST, depending on desired client behavior
public class JobConfigurationDeletionException extends RuntimeException {
    public JobConfigurationDeletionException(String message) {
        super(message);
    }
}
