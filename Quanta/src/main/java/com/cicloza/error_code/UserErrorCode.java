package com.cicloza.error_code;

/**
 * Enumeration of error codes specific to user-related operations.
 * Implements CiclozaErrorCode interface to provide standardized error code handling.
 * Each error code has a unique numeric identifier and descriptive documentation.
 */
public enum UserErrorCode implements CiclozaErrorCode {
    /**
     * Error code for when the first name is required.
     * Code: 40001
     */
    FIRST_NAME_REQUIRED(40001),
    /**
     * Error code for when the last name is required.
     * Code: 40002
     */
    LAST_NAME_REQUIRED(40002),
    /**
     * Error code for when the phone number is required.
     * Code: 40003
     */
    PHONE_REQUIRED(40003),
    /**
     * Error code for when the email is required.
     * Code: 40004
     */
    EMAIL_REQUIRED(40004),
    /**
     * Error code for when the role is required.
     * Code: 40005
     */
    ROLE_REQUIRED(40005),
    /**
     * Error code for when the password is required.
     * Code: 40006
     */
    PASSWORD_REQUIRED(40006),
    /**
     * Error code for when the phone number already exists.
     * Code: 40007
     */
    PHONE_ALREADY_EXISTS(40007),
    /**
     * Error code for when the email already exists.
     * Code: 40008
     */
    EMAIL_ALREADY_EXISTS(40008),
    /**
     * Error code for internal server error.
     * Code: 40009
     */
    INTERNAL_SERVER_ERROR(40009),
    /**
     * Error code for when the user is not found.
     * Code: 40010
     */
    USER_NOT_FOUND(40010),
    /**
     * Error code for when the user ID is required.
     * Code: 40011
     */
    USER_ID_REQUIRED(40011),
    /**
     * Error code for when the UUID format is invalid.
     * Code: 40012
     */
    INVALID_UUID_FORMAT(40012),
    /**
     * Error code for when the email format is invalid.
     * Code: 40013
     */
    INVALID_EMAIL_FORMAT(40013),
    /**
     * Error code for when the phone format is invalid.
     * Code: 40014
     */
    INVALID_PHONE_FORMAT(40014);

    /**
     * The numeric code associated with this error code.
     * This code is used in error responses and documentation.
     */
    private final int codeNumber;

    /**
     * Constructor for UserErrorCode enum.
     * 
     * @param codeNumber The numeric code to associate with this error code
     */
    UserErrorCode(int codeNumber) {
        this.codeNumber = codeNumber;
    }

    /**
     * Retrieves the numeric code for this error code.
     * 
     * @return The numeric code associated with this error code
     */
    @Override
    public int getCode() {
        return codeNumber;
    }
}