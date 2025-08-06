package com.cicloza.exception;

import com.cicloza.error_code.UserErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Exception class specifically for user-related errors in the Cicloza application.
 * Extends RuntimeException and includes both an error code and HTTP status code.
 * Used to handle and communicate user-specific error conditions.
 */
@RequiredArgsConstructor
@Getter
@Setter
public class UserException extends RuntimeException {
    /**
     * The specific error code associated with this user exception.
     */
    private UserErrorCode errorCode;

    /**
     * The HTTP status code associated with this exception.
     */
    private int statusCode;

    /**
     * Constructs a new UserException with a message, error code, and status code.
     * 
     * @param message The detail message explaining the error
     * @param errorCode The specific error code for this exception
     * @param statusCode The HTTP status code for this error
     */
    public UserException(String message, UserErrorCode errorCode, int statusCode) {
        super(message);
        this.errorCode = errorCode;
        this.statusCode = statusCode;
    }

    /**
     * Constructs a new UserException with a message, error code, and cause.
     * 
     * @param message The detail message explaining the error
     * @param errorCode The specific error code for this exception
     * @param cause The cause of this exception
     */
    public UserException(String message, UserErrorCode errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}