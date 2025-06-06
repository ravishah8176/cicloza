package com.cicloza.dto;

import com.cicloza.error_code.UserErrorCode;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object (DTO) for user-specific error responses.
 * Used to format error responses related to user operations in a consistent way.
 * Includes timestamp, HTTP status, error code, and descriptive message.
 */
@Getter
@Setter
@Data
public class UserErrorResponse {
    /**
     * The exact time when the error occurred.
     */
    private LocalDateTime timestamp;

    /**
     * HTTP status code indicating the type of error.
     */
    private int status;

    /**
     * Specific error code from UserErrorCode enum identifying the user-related error.
     */
    private UserErrorCode errorCode;

    /**
     * Detailed error message explaining what went wrong.
     */
    private String message;

    /**
     * Constructor for creating a UserErrorResponse with all required fields.
     * 
     * @param timestamp The time when the error occurred
     * @param status The HTTP status code
     * @param errorCode The specific error code from UserErrorCode
     * @param message The detailed error message
     */
    public UserErrorResponse(LocalDateTime timestamp, int status, UserErrorCode errorCode, String message) {
        this.timestamp = timestamp;
        this.status = status;
        this.errorCode = errorCode;
        this.message = message;
    }
} 