package com.college.digitalparcel.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/*
 * WHAT IS THIS CLASS?
 * This is a global error handler for the entire application.
 *
 * WITHOUT this class:
 *   Every controller would need its own try-catch blocks.
 *   Error responses would be inconsistent across the app.
 *   Stack traces might leak to the client.
 *
 * WITH this class:
 *   When any exception is thrown anywhere in the app, it comes here.
 *   We catch it and return a clean, consistent JSON error response.
 *   The client always gets a proper error message, never a stack trace.
 *
 * @RestControllerAdvice means:
 *   "Watch all controllers. If any of them throw an exception, handle it here."
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /*
     * @ExceptionHandler(SomeException.class) means:
     *   "When SomeException is thrown anywhere, run this method"
     */

    // Handles 404 Not Found — when a parcel or student doesn't exist
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // Handles 400 Bad Request — when input data is invalid (e.g. duplicate register number)
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(BadRequestException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /*
     * Handles validation errors from @Valid annotation.
     * When a request body fails validation (e.g. @NotBlank field is empty),
     * this method returns all the field-level error messages.
     *
     * Example response:
     * {
     *   "errors": {
     *     "name": "Name is required",
     *     "email": "Email must be valid"
     *   }
     * }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        // Collect all field errors into a map: fieldName → errorMessage
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("errors", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // Handles 403 Forbidden — user is logged in but doesn't have permission
    // Example: a student trying to create a parcel
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, "Access denied: you don't have permission to perform this action");
    }

    // Handles 401 Unauthorized — wrong username or password during login
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid username or password");
    }

    // Catches any other unexpected exception
    // This prevents stack traces from being sent to the client
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    // Helper method to build a consistent error response body
    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
