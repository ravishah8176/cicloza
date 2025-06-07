package com.cicloza.service;

import com.cicloza.entity.User;
import com.cicloza.error_code.UserErrorCode;
import com.cicloza.exception.UserException;
import com.cicloza.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoderService passwordEncoderService;
    Logger logger = org.slf4j.LoggerFactory.getLogger(UserService.class);

    /**
     * Retrieves all users from the database.
     * 
     * @return List of all Users in the system
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Retrieves a single user by their UUID.
     * 
     * @param id UUID of the user to find
     * @return Users object if found
     * @throws UserException if user not found
     */
    public User getUserById(UUID id) {
        logger.info("Fetching user with ID: {}", id);
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            return user.get();
        } else {
            logger.warn("User not found with ID: {}", id);
            throw new UserException("User not found with ID: " + id, UserErrorCode.USER_NOT_FOUND, HttpStatus.NOT_FOUND.value());
        }
    }

    /**
     * Creates a new user in the database.
     * 
     * @param user Users object containing user details
     * @return Created Users object
     * @throws UserException if validation fails
     */
    public User createUser(User user) throws UserException {
        logger.info("Creating new user with email: {}", user.getEmail());

        validateRequiredFields(user);
        validateUniqueConstraints(user);

        // Hash the password before saving
        user.setPassword(passwordEncoderService.encode(user.getPassword()));

        logger.debug("User validation successful, proceeding with save");
        return userRepository.save(user);
    }

    /**
     * Updates an existing user's information.
     * 
     * @param userId UUID of the user to update
     * @param updateRequest Users object containing updated fields
     * @return Updated Users object
     */
    public User updateUser(UUID userId, User updateRequest) {
        logger.info("Processing update request for user ID: {}", userId);
        
        User existingUser = getUserById(userId);
        validateUpdateRequest(updateRequest);
        
        updateUserFields(existingUser, updateRequest);
        
        // If password is being updated, hash it
        if (updateRequest.getPassword() != null) {
            existingUser.setPassword(passwordEncoderService.encode(updateRequest.getPassword()));
        }
        
        return userRepository.save(existingUser);
    }

    /**
     * Deletes a user from the database.
     * 
     * @param id UUID of the user to delete
     */
    public void deleteUser(UUID id) {
        logger.info("Deleting user with ID: {}", id);
        User user = getUserById(id);
        userRepository.delete(user);
    }

    /**
     * Validates the update request for a user.
     * 
     * @param updateRequest Users object containing update data
     * @throws UserException if validation fails
     */
    private void validateUpdateRequest(User updateRequest) {
        if (updateRequest.getEmail() != null && !isValidEmail(updateRequest.getEmail())) {
            throw new UserException("Invalid email format", UserErrorCode.INVALID_EMAIL_FORMAT, HttpStatus.BAD_REQUEST.value());
        }
        
        if (updateRequest.getPhone() != null && !isValidPhone(updateRequest.getPhone())) {
            throw new UserException("Invalid phone format", UserErrorCode.INVALID_PHONE_FORMAT, HttpStatus.BAD_REQUEST.value());
        }
        
        // Check for unique constraints if email or phone is being updated
        if (updateRequest.getEmail() != null) {
            userRepository.findByEmail(updateRequest.getEmail())
                .ifPresent(user -> {
                    if (!user.getUserId().equals(updateRequest.getUserId())) {
                        throw new UserException("Email already exists", UserErrorCode.EMAIL_ALREADY_EXISTS, HttpStatus.CONFLICT.value());
                    }
                });
        }
        
        if (updateRequest.getPhone() != null) {
            userRepository.findByPhone(updateRequest.getPhone())
                .ifPresent(user -> {
                    if (!user.getUserId().equals(updateRequest.getUserId())) {
                        throw new UserException("Phone number already exists", UserErrorCode.PHONE_ALREADY_EXISTS, HttpStatus.CONFLICT.value());
                    }
                });
        }
    }

    /**
     * Updates fields of existing user with non-null values from update request.
     * 
     * @param existingUser Current user object to be updated
     * @param updateRequest Users object containing new values
     */
    private void updateUserFields(User existingUser, User updateRequest) {
        if (updateRequest.getEmail() != null) {
            existingUser.setEmail(updateRequest.getEmail());
        }
        
        if (updateRequest.getFirstName() != null) {
            existingUser.setFirstName(updateRequest.getFirstName());
        }
        
        if (updateRequest.getLastName() != null) {
            existingUser.setLastName(updateRequest.getLastName());
        }
        
        if (updateRequest.getPhone() != null) {
            existingUser.setPhone(updateRequest.getPhone());
        }
        
        if (updateRequest.getRole() != null) {
            existingUser.setRole(updateRequest.getRole());
        }
    }

    /**
     * Validates email format.
     * 
     * @param email Email string to validate
     * @return boolean indicating if email is valid
     */
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    /**
     * Validates phone number format.
     * 
     * @param phone Phone number string to validate
     * @return boolean indicating if phone number is valid
     */
    private boolean isValidPhone(String phone) {
        return phone != null && phone.matches("^\\+?[1-9]\\d{1,14}$");
    }

    /**
     * Validates that all required fields are present in user object.
     * 
     * @param user Users object to validate
     * @throws UserException if required fields are missing
     */
    private void validateRequiredFields(User user) throws UserException {
        if (isNullOrEmpty(user.getEmail())) {
            throw new UserException("Email is required", UserErrorCode.EMAIL_REQUIRED, HttpStatus.BAD_REQUEST.value());
        }
        if (isNullOrEmpty(user.getFirstName())) {
            throw new UserException("First name is required", UserErrorCode.FIRST_NAME_REQUIRED, HttpStatus.BAD_REQUEST.value());
        }
        if (isNullOrEmpty(user.getPassword())) {
            throw new UserException("Password is required", UserErrorCode.PASSWORD_REQUIRED, HttpStatus.BAD_REQUEST.value());
        }
        if (isNullOrEmpty(user.getPhone())) {
            throw new UserException("Phone number is required", UserErrorCode.PHONE_REQUIRED, HttpStatus.BAD_REQUEST.value());
        }
        if (isNullOrEmpty(user.getRole())) {
            throw new UserException("Role is required", UserErrorCode.ROLE_REQUIRED, HttpStatus.BAD_REQUEST.value());
        }
    }

    /**
     * Validates that email and phone are unique in the database.
     * 
     * @param user Users object to validate
     * @throws UserException if unique constraints are violated
     */
    private void validateUniqueConstraints(User user) throws UserException {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            logger.warn("Attempt to create user with existing email: {}", user.getEmail());
            throw new UserException("Email already exists", UserErrorCode.EMAIL_ALREADY_EXISTS, HttpStatus.CONFLICT.value());
        }
        if (userRepository.findByPhone(user.getPhone()).isPresent()) {
            logger.warn("Attempt to create user with existing phone: {}", user.getPhone());
            throw new UserException("Phone number already exists", UserErrorCode.PHONE_ALREADY_EXISTS, HttpStatus.CONFLICT.value());
        }
    }

    /**
     * Checks if a string is null or empty.
     * 
     * @param value String to check
     * @return boolean indicating if string is null or empty
     */
    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}