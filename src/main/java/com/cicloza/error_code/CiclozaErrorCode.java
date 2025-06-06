package com.cicloza.error_code;

/**
 * Interface defining the contract for error codes in the Cicloza application.
 * Implemented by specific error code enums to provide standardized error code handling.
 * Ensures consistent error code retrieval across different types of errors.
 */
public interface CiclozaErrorCode {

    /**
     * Retrieves the numeric code associated with this error.
     * 
     * @return The integer value representing this error code
     */
    int getCode();
}
