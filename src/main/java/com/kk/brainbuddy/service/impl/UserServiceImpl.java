package com.kk.brainbuddy.service.impl;

import com.kk.brainbuddy.dto.UserLoginDTO;
import com.kk.brainbuddy.dto.UserRegistrationDTO;
import com.kk.brainbuddy.entity.User;
import com.kk.brainbuddy.repository.UserRepository;
import com.kk.brainbuddy.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementation of UserService
 */
@Service
public class UserServiceImpl implements UserService {
    
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public User registerUser(UserRegistrationDTO registrationDTO) {
        log.info("Registering new user with email: {}", registrationDTO.getEmail());
        
        // Check if email already exists
        if (emailExists(registrationDTO.getEmail())) {
            throw new RuntimeException("Email already exists: " + registrationDTO.getEmail());
        }
        
        // Create new user
        User user = User.builder()
                .name(registrationDTO.getName())
                .email(registrationDTO.getEmail())
                .password(registrationDTO.getPassword()) // In production, hash this password
                .bio(registrationDTO.getBio())
                .build();
        
        User savedUser = userRepository.save(user);
        log.info("Successfully registered user with ID: {}", savedUser.getId());
        
        return savedUser;
    }

    @Override
    public Optional<User> authenticateUser(UserLoginDTO loginDTO) {
        log.info("Authenticating user with email: {}", loginDTO.getEmail());
        
        Optional<User> userOpt = userRepository.findByEmail(loginDTO.getEmail());
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // In production, use proper password hashing (BCrypt)
            if (user.getPassword().equals(loginDTO.getPassword())) {
                log.info("Authentication successful for user: {}", loginDTO.getEmail());
                return Optional.of(user);
            } else {
                log.warn("Authentication failed - invalid password for user: {}", loginDTO.getEmail());
            }
        } else {
            log.warn("Authentication failed - user not found: {}", loginDTO.getEmail());
        }
        
        return Optional.empty();
    }

    @Override
    public boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}