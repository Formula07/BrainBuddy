package com.kk.brainbuddy.controller;

import com.kk.brainbuddy.dto.SwipeRequestDTO;
import com.kk.brainbuddy.dto.SwipeResultDTO;
import com.kk.brainbuddy.dto.UserProfileDTO;
import com.kk.brainbuddy.entity.User;
import com.kk.brainbuddy.exception.DuplicateSwipeException;
import com.kk.brainbuddy.exception.UserNotFoundException;
import com.kk.brainbuddy.service.SwipeResult;
import com.kk.brainbuddy.service.SwipeService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * REST Controller for handling swipe operations
 * Requirements: 1.1, 1.2, 2.1, 2.2, 2.5, 6.2
 */
@RestController
@RequestMapping("/api/swipes")
public class SwipeController {
    
    private static final Logger log = LoggerFactory.getLogger(SwipeController.class);
    private final SwipeService swipeService;

    public SwipeController(SwipeService swipeService) {
        this.swipeService = swipeService;
    }
    
    /**
     * Get next potential match for a user
     * Requirements: 1.1, 1.2
     * 
     * @param userId the ID of the user looking for matches
     * @return ResponseEntity containing UserProfileDTO or 404 if no matches available
     */
    @GetMapping("/potential/{userId}")
    public ResponseEntity<UserProfileDTO> getNextPotentialMatch(@PathVariable Long userId) {
        log.info("Getting next potential match for user: {}", userId);
        
        try {
            Optional<User> potentialMatch = swipeService.getNextPotentialMatch(userId);
            
            if (potentialMatch.isPresent()) {
                UserProfileDTO userProfileDTO = mapToUserProfileDTO(potentialMatch.get());
                log.info("Found potential match for user {}: {}", userId, userProfileDTO.getId());
                return ResponseEntity.ok(userProfileDTO);
            } else {
                log.info("No potential matches available for user: {}", userId);
                return ResponseEntity.notFound().build();
            }
            
        } catch (UserNotFoundException e) {
            log.error("User not found: {}", userId, e);
            throw e;
        } catch (Exception e) {
            log.error("Error getting potential match for user: {}", userId, e);
            throw new RuntimeException("Error retrieving potential match", e);
        }
    }
    
    /**
     * Record a swipe action
     * Requirements: 2.1, 2.2, 2.5, 6.2
     * 
     * @param swipeRequest the swipe request containing swiper ID, target ID, and liked status
     * @return ResponseEntity containing SwipeResultDTO with operation result
     */
    @PostMapping
    public ResponseEntity<SwipeResultDTO> recordSwipe(@Valid @RequestBody SwipeRequestDTO swipeRequest) {
        log.info("Recording swipe: swiper={}, target={}, liked={}", 
                swipeRequest.getSwiperId(), swipeRequest.getTargetId(), swipeRequest.getLiked());
        
        try {
            SwipeResult result = swipeService.recordSwipe(
                    swipeRequest.getSwiperId(),
                    swipeRequest.getTargetId(),
                    swipeRequest.getLiked()
            );
            
            SwipeResultDTO responseDTO = mapToSwipeResultDTO(result);
            
            if (result.isSuccess()) {
                log.info("Swipe recorded successfully. Match created: {}", result.isMatch());
                return ResponseEntity.ok(responseDTO);
            } else {
                log.warn("Swipe recording failed: {}", result.getMessage());
                return ResponseEntity.badRequest().body(responseDTO);
            }
            
        } catch (UserNotFoundException e) {
            log.error("User not found during swipe operation", e);
            throw e;
        } catch (DuplicateSwipeException e) {
            log.error("Duplicate swipe attempt", e);
            throw e;
        } catch (Exception e) {
            log.error("Error recording swipe", e);
            throw new RuntimeException("Error recording swipe", e);
        }
    }
    
    /**
     * Map User entity to UserProfileDTO
     * 
     * @param user the User entity
     * @return UserProfileDTO
     */
    private UserProfileDTO mapToUserProfileDTO(User user) {
        return UserProfileDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .bio(user.getBio())
                .build();
    }
    
    /**
     * Map SwipeResult to SwipeResultDTO
     * 
     * @param result the SwipeResult
     * @return SwipeResultDTO
     */
    private SwipeResultDTO mapToSwipeResultDTO(SwipeResult result) {
        UserProfileDTO nextMatch = null;
        if (result.getNextPotentialMatch() != null) {
            nextMatch = mapToUserProfileDTO(result.getNextPotentialMatch());
        }
        
        return SwipeResultDTO.builder()
                .success(result.isSuccess())
                .isMatch(result.isMatch())
                .nextPotentialMatch(nextMatch)
                .message(result.getMessage())
                .build();
    }
}