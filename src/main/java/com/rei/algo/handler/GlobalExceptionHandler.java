package com.rei.algo.handler;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException; // Base class for auth errors
import org.springframework.web.bind.MethodArgumentNotValidException; // For @Valid validation errors
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice // Indicates this class provides centralized exception handling for controllers
@Slf4j
@Hidden
public class GlobalExceptionHandler {

    // Simple Error Response DTO
    @Data
    @AllArgsConstructor
    private static class ErrorResponse {
        private LocalDateTime timestamp;
        private int status;
        private String error;
        private String message;
        private String path; // Can get path from request if needed
        private Map<String, String> validationErrors; // For validation errors
    }

    // Handle Validation Errors (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        log.warn("Validation error: {}", errors);
        return new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                "Input data validation failed",
                null, // TODO: Get request path if necessary
                errors
        );
    }

    // Handle Access Denied (Authorization Failed - 403 Forbidden)
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access Denied: {}", ex.getMessage());
        return new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                ex.getMessage(),
                null, null
        );
    }

    // Handle Authentication Failures (e.g., bad credentials - 401 Unauthorized)
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleAuthenticationException(AuthenticationException ex) {
        log.warn("Authentication Failed: {}", ex.getMessage());
        String message = "用户名或密码错误";
        if (ex.getMessage() != null && ex.getMessage().contains("Bad credentials")) {
            message = "用户名或密码错误";
        }
        return new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                message,
                null, null
        );
    }

     // Handle custom or specific runtime exceptions from services (e.g., ResourceNotFound)
     @ExceptionHandler(RuntimeException.class) // Catching broad RuntimeException first
     public ResponseEntity<ErrorResponse> handleServiceExceptions(RuntimeException ex) {
         HttpStatus status = HttpStatus.BAD_REQUEST; // Default to 400 Bad Request for general service errors
         String errorType = "Bad Request";

         // Example: Check for specific custom exception types if you define them
         // if (ex instanceof ResourceNotFoundException) {
         //     status = HttpStatus.NOT_FOUND;
         //     errorType = "Not Found";
         // } else if (ex instanceof DataConflictException) {
         //     status = HttpStatus.CONFLICT;
         //     errorType = "Conflict";
         // }

         // Log the full exception for internal debugging
         log.error("Unhandled RuntimeException: {}", ex.getMessage(), ex);

         ErrorResponse errorResponse = new ErrorResponse(
                 LocalDateTime.now(),
                 status.value(),
                 errorType,
                 ex.getMessage(), // Use the exception message directly
                 null, null
         );
         return new ResponseEntity<>(errorResponse, status);
     }


    // Handle generic Exceptions (Catch-all - 500 Internal Server Error)
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleAllUncaughtException(Exception ex) {
        log.error("An unexpected error occurred: {}", ex.getMessage(), ex);
        return new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred. Please try again later.", // Generic message for users
                null, null
        );
    }
}
