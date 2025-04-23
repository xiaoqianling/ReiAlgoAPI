package com.rei.algo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Note: Naming conflict with org.springframework.security.access.AccessDeniedException.
// Using a custom exception allows for specific handling, but be mindful of potential confusion.
// Consider if a different name might be clearer if using Spring Security extensively.
@ResponseStatus(value = HttpStatus.FORBIDDEN) // Map this exception to 403 Forbidden HTTP status
public class AccessDeniedException extends RuntimeException {

    private static final long serialVersionUID = 2L;

    public AccessDeniedException(String message) {
        super(message);
    }

    public AccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
} 