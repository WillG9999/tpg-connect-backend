package com.tpg.connect.common.exceptions;

import com.tpg.connect.admin.exception.ApplicationAlreadyProcessedException;
import com.tpg.connect.admin.exception.ApplicationNotFoundException;
import com.tpg.connect.admin.exception.UserCreationFailedException;
import com.tpg.connect.email_verification.exceptions.EmailVerificationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Bad credentials: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "error", "Unauthorized",
                        "message", ex.getMessage(),
                        "timestamp", Instant.now().toString()
                ));
    }

    @ExceptionHandler(EmailVerificationException.class)
    public ResponseEntity<Map<String, Object>> handleEmailVerificationException(EmailVerificationException ex) {
        log.warn("Email verification failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", "Verification Failed",
                        "message", ex.getMessage(),
                        "timestamp", Instant.now().toString()
                ));
    }

    @ExceptionHandler(MissingAuthorizationHeaderException.class)
    public ResponseEntity<Map<String, Object>> handleMissingAuthHeader(MissingAuthorizationHeaderException ex) {
        log.warn("Missing authorization header");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "error", "Unauthorized",
                        "message", "Missing or invalid Authorization header",
                        "timestamp", Instant.now().toString()
                ));
    }

    @ExceptionHandler(ApplicationNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleApplicationNotFound(ApplicationNotFoundException ex) {
        log.warn("Application not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "error", "Not Found",
                        "message", ex.getMessage(),
                        "timestamp", Instant.now().toString()
                ));
    }

    @ExceptionHandler(ApplicationAlreadyProcessedException.class)
    public ResponseEntity<Map<String, Object>> handleApplicationAlreadyProcessed(ApplicationAlreadyProcessedException ex) {
        log.warn("Application already processed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", "Bad Request",
                        "message", ex.getMessage(),
                        "timestamp", Instant.now().toString()
                ));
    }

    @ExceptionHandler(UserCreationFailedException.class)
    public ResponseEntity<Map<String, Object>> handleUserCreationFailed(UserCreationFailedException ex) {
        log.error("User creation failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "error", "Internal Server Error",
                        "message", ex.getMessage(),
                        "timestamp", Instant.now().toString()
                ));
    }
}

