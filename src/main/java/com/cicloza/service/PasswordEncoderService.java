package com.cicloza.service;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import org.springframework.stereotype.Service;

@Service
public class PasswordEncoderService {
    private final Argon2 argon2;

    public PasswordEncoderService() {
        // Using Argon2id variant which is recommended for password hashing
        this.argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
    }

    /**
     * Hashes a password using Argon2id
     * 
     * @param password The plain text password to hash
     * @return The hashed password
     */
    public String encode(String password) {
        // Parameters:
        // iterations: 10 - number of iterations
        // memory: 65536 - memory usage in KiB (64 MiB)
        // parallelism: 1 - number of parallel threads
        // saltLength: 16 - length of the salt in bytes
        // hashLength: 32 - length of the hash in bytes
        return argon2.hash(10, 65536, 1, password.toCharArray());
    }

    /**
     * Verifies if a plain text password matches a hashed password
     * 
     * @param plainPassword The plain text password to verify
     * @param hashedPassword The hashed password to compare against
     * @return true if the passwords match, false otherwise
     */
    public boolean verify(String plainPassword, String hashedPassword) {
        return argon2.verify(hashedPassword, plainPassword.toCharArray());
    }
}
