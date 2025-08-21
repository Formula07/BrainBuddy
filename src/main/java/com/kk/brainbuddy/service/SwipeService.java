package com.kk.brainbuddy.service;

import com.kk.brainbuddy.entity.User;

import java.util.Optional;

/**
 * Service interface for handling swipe operations
 * Requirements: 1.1, 1.3, 2.1, 2.2, 2.4, 5.1, 5.3
 */
public interface SwipeService {
    
    /**
     * Get the next potential match for a user
     * Requirements: 1.1, 5.1
     * 
     * @param userId the ID of the user looking for matches
     * @return Optional containing the next potential match, or empty if no matches available
     */
    Optional<User> getNextPotentialMatch(Long userId);
    
    /**
     * Record a swipe action and check for mutual matches
     * Requirements: 2.1, 2.2, 2.4, 5.3
     * 
     * @param swiperId the ID of the user performing the swipe
     * @param targetId the ID of the user being swiped on
     * @param liked true if liked (swipe right), false if disliked (swipe left)
     * @return SwipeResult containing operation result and next potential match
     */
    SwipeResult recordSwipe(Long swiperId, Long targetId, boolean liked);
    
    /**
     * Check if a user has already swiped on a target user
     * Requirements: 2.3, 5.1
     * 
     * @param swiperId the ID of the user who might have swiped
     * @param targetId the ID of the target user
     * @return true if the user has already swiped on the target
     */
    boolean hasUserSwipedOnTarget(Long swiperId, Long targetId);
}