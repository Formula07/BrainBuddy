package com.kk.brainbuddy.service;

import com.kk.brainbuddy.dto.UserLoginDTO;
import com.kk.brainbuddy.dto.UserRegistrationDTO;
import com.kk.brainbuddy.entity.User;

import java.util.Optional;

/**
 * Service interface for user management
 */
public interface UserService {
    
    /**
     * Register a new user
     */
    User registerUser(UserRegistrationDTO registrationDTO);
    
    /**
     * Authenticate user login
     */
    Optional<User> authenticateUser(UserLoginDTO loginDTO);
    
    /**
     * Check if email already exists
     */
    boolean emailExists(String email);
    
    /**
     * Get user by ID
     */
    Optional<User> getUserById(Long id);
    
    /**
     * Get user by email
     */
    Optional<User> getUserByEmail(String email);
}