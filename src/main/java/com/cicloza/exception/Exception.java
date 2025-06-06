package com.cicloza.exception;

import com.cicloza.error_code.UserErrorCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Base exception class for the Cicloza application.
 * Extends Java's standard Exception class and includes a UserErrorCode for standardized error handling.
 * Used as a foundation for application-specific exceptions.
 */
@Getter
@Setter
public class Exception extends java.lang.Exception {
    /**
     * The specific error code associated with this exception.
     */
    private UserErrorCode errorCode;

    /**
     * Constructs a new Exception with a message and error code.
     * 
     * @param message The detail message explaining the error
     * @param errorCode The specific error code for this exception
     */
    public Exception(String message, UserErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Constructs a new Exception with a message, error code, and cause.
     * 
     * @param message The detail message explaining the error
     * @param errorCode The specific error code for this exception
     * @param cause The cause of this exception
     */
    public Exception(String message, UserErrorCode errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
