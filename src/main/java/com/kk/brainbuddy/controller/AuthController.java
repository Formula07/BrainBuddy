package com.kk.brainbuddy.controller;

import com.kk.brainbuddy.dto.*;
import com.kk.brainbuddy.entity.User;
import com.kk.brainbuddy.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * REST Controller for authentication operations
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Register a new user
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody UserRegistrationDTO registrationDTO) {
        log.info("Registration request for email: {}", registrationDTO.getEmail());
        
        try {
            // Check if email already exists
            if (userService.emailExists(registrationDTO.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(AuthResponseDTO.builder()
                                .success(false)
                                .message("Email already exists")
                                .build());
            }
            
            // Register user
            User user = userService.registerUser(registrationDTO);
            
            // Convert to UserProfileDTO
            UserProfileDTO userProfile = UserProfileDTO.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .bio(user.getBio())
                    .build();
            
            return ResponseEntity.ok(AuthResponseDTO.builder()
                    .success(true)
                    .message("Registration successful")
                    .user(userProfile)
                    .token("dummy-token") // In production, generate JWT token
                    .build());
            
        } catch (Exception e) {
            log.error("Registration failed for email: {}", registrationDTO.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthResponseDTO.builder()
                            .success(false)
                            .message("Registration failed: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Login user
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody UserLoginDTO loginDTO) {
        log.info("Login request for email: {}", loginDTO.getEmail());
        
        try {
            Optional<User> userOpt = userService.authenticateUser(loginDTO);
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                // Convert to UserProfileDTO
                UserProfileDTO userProfile = UserProfileDTO.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .bio(user.getBio())
                        .build();
                
                return ResponseEntity.ok(AuthResponseDTO.builder()
                        .success(true)
                        .message("Login successful")
                        .user(userProfile)
                        .token("dummy-token") // In production, generate JWT token
                        .build());
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(AuthResponseDTO.builder()
                                .success(false)
                                .message("Invalid email or password")
                                .build());
            }
            
        } catch (Exception e) {
            log.error("Login failed for email: {}", loginDTO.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthResponseDTO.builder()
                            .success(false)
                            .message("Login failed: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Check if email exists
     */
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        boolean exists = userService.emailExists(email);
        return ResponseEntity.ok(exists);
    }
}