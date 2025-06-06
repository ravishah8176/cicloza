package com.cicloza.repository;

import com.cicloza.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for managing Users in the database.
 * Extends JpaRepository to provide basic CRUD operations and custom query methods.
 */
public interface UserRepository extends JpaRepository<User, UUID> {
    /**
     * Finds a user by their email address.
     * 
     * @param email The email address to search for
     * @return Optional containing the Users object if found, empty Optional if not found
     */
    Optional<User> findByEmail(String email);

    /**
     * Finds a user by their phone number.
     * 
     * @param phone The phone number to search for
     * @return Optional containing the Users object if found, empty Optional if not found
     */
    Optional<User> findByPhone(String phone);
} 