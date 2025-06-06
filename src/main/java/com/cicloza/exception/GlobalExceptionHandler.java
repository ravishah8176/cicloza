package com.cicloza.exception;

import com.cicloza.dto.UserErrorResponse;
import com.cicloza.error_code.UserErrorCode;
import com.cicloza.util.ColorLogger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;

/**
 * Global exception handler for the application.
 * Provides centralized exception handling across all controllers.
 * Converts exceptions into appropriate HTTP responses with error details.
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    private static final ColorLogger logger = ColorLogger.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles MethodArgumentTypeMismatchException, specifically for UUID format validation.
     * 
     * @param ex The MethodArgumentTypeMismatchException that was thrown
     * @return ResponseEntity containing a UserErrorResponse with appropriate error details
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        if (ex.getRequiredType() == java.util.UUID.class) {
            logger.error("Invalid UUID format: {}", ex.getValue());
            UserErrorResponse error = new UserErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                UserErrorCode.INVALID_UUID_FORMAT,
                "Invalid UUID format: " + ex.getValue()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    /**
     * Handles UserException, converting it into a standardized error response.
     * 
     * @param ex The UserException that was thrown
     * @return ResponseEntity containing a UserErrorResponse with error details
     */
    @ExceptionHandler(UserException.class)
    public ResponseEntity<?> handleUserException(UserException ex) {
        logger.error("UserException occurred: {}", ex.getMessage());
        UserErrorResponse error = new UserErrorResponse(
            LocalDateTime.now(),
            ex.getStatusCode(),
            ex.getErrorCode(),
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
} 