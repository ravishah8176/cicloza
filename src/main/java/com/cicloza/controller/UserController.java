package com.cicloza.controller;

import com.cicloza.entity.User;
import com.cicloza.exception.UserException;
import com.cicloza.service.UserService;
import com.cicloza.util.ColorLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for handling user-related HTTP requests.
 * Provides endpoints for CRUD operations on users.
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private static final ColorLogger logger = ColorLogger.getLogger(UserController.class);
    private final UserService userService;

    /**
     * Retrieves all users from the system.
     * 
     * @return ResponseEntity containing a list of all users
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * Retrieves a specific user by their ID.
     * 
     * @param id UUID of the user to retrieve
     * @return ResponseEntity containing the user if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable UUID id) {
        try {
            User user = userService.getUserById(id);
            return ResponseEntity.status(HttpStatus.OK).body(user);
        } catch (Exception ex) {
            logger.error("Inside getUserById Exception: {}", ex.getMessage());
            throw ex;
        }
    }

    /**
     * Creates a new user in the system.
     * 
     * @param user User object containing the details of the user to create
     * @return ResponseEntity containing the created user
     */
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            logger.info("Creating user with email: {}", user.getEmail());
            User createdUser = userService.createUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (Exception ex) {
            logger.error("Unexpected error while creating user: {}", ex.getMessage());
            throw ex;
        }
    }

    /**
     * Updates an existing user's information.
     * 
     * @param id UUID of the user to update
     * @param user User object containing the updated information
     * @return ResponseEntity containing the updated user
     * @throws UserException if update fails
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable UUID id, @RequestBody User user) {
        logger.info("Updating user with ID: {}", id);
        User updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Deletes a user from the system.
     * 
     * @param id UUID of the user to delete
     * @return ResponseEntity with no content if deletion is successful
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }
} 