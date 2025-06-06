package com.cicloza.dto;

import com.cicloza.error_code.UserErrorCode;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object (DTO) for standardized exception responses.
 * Used to format error responses in a consistent way across the application.
 * Includes timestamp, HTTP status, error message, and specific error code.
 */
@Setter
@Getter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionResponse {
    /**
     * HTTP status code of the error response.
     */
    private int status;

    /**
     * Timestamp when the error occurred.
     */
    private LocalDateTime timestamp;

    /**
     * Human-readable error message describing the exception.
     */
    private String message;

    /**
     * Specific error code from UserErrorCode enum identifying the type of error.
     */
    private UserErrorCode errorCode;

    /**
     * Constructor for creating an ExceptionResponse with all required fields.
     * 
     * @param timestamp The time when the error occurred
     * @param status The HTTP status code
     * @param error The specific error code from UserErrorCode
     * @param message The error message
     */
    public ExceptionResponse(LocalDateTime timestamp, int status, UserErrorCode error, String message) {
        this.timestamp = timestamp;
        this.status = status;
        this.errorCode = error;
        this.message = message;
    }
}
