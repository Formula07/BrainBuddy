package com.kk.brainbuddy.controller;

import com.kk.brainbuddy.dto.UserProfileDTO;
import com.kk.brainbuddy.entity.User;
import com.kk.brainbuddy.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * REST Controller for user operations
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserProfileDTO> getUserById(@PathVariable Long id) {
        Optional<User> userOpt = userService.getUserById(id);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            UserProfileDTO userProfile = UserProfileDTO.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .bio(user.getBio())
                    .build();
            return ResponseEntity.ok(userProfile);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserProfileDTO> getUserByEmail(@PathVariable String email) {
        Optional<User> userOpt = userService.getUserByEmail(email);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            UserProfileDTO userProfile = UserProfileDTO.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .bio(user.getBio())
                    .build();
            return ResponseEntity.ok(userProfile);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
