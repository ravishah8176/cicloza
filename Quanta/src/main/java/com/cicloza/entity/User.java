package com.cicloza.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

/**
 * Entity class representing a user in the system.
 * Maps to the 'users' table in the database.
 * Contains user information including personal details and authentication data.
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    /**
     * Unique identifier for the user.
     * Automatically generated using UUID.
     * Cannot be updated once set.
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "user_id", updatable = false, nullable = false)
    private UUID userId;

    /**
     * User's first name.
     * Required field.
     */
    @Column(name = "first_name", nullable = false)
    private String firstName;

    /**
     * User's last name.
     * Optional field.
     */
    @Column(name = "last_name")
    private String lastName;

    /**
     * User's phone number.
     * Required field and must be unique across all users.
     */
    @Column(name = "phone", nullable = false, unique = true)
    private String phone;

    /**
     * User's email address.
     * Required field and must be unique across all users.
     */
    @Column(name = "email_id", nullable = false, unique = true)
    private String email;

    /**
     * User's role in the system.
     * Required field.
     */
    @Column(name = "role", nullable = false)
    private String role;

    /**
     * User's password.
     * Required field.
     * Should be stored in encrypted format.
     */
    @Column(name = "password", nullable = false)
    private String password;
} 