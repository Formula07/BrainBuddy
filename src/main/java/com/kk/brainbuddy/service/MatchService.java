package com.kk.brainbuddy.service;

import com.kk.brainbuddy.entity.Match;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for handling match operations
 * Requirements: 3.1, 3.3, 3.4, 4.1, 4.3
 */
public interface MatchService {
    
    /**
     * Create a match if two users have mutually liked each other
     * Requirements: 3.1, 3.3
     * 
     * @param user1Id the ID of the first user
     * @param user2Id the ID of the second user
     * @return Optional containing the created match, or empty if no mutual match exists
     */
    Optional<Match> createMatchIfMutual(Long user1Id, Long user2Id);
    
    /**
     * Get all matches for a user
     * Requirements: 4.1
     * 
     * @param userId the ID of the user
     * @return List of matches for the user
     */
    List<Match> getUserMatches(Long userId);
    
    /**
     * Check if two users are already matched
     * Requirements: 3.3, 4.3
     * 
     * @param user1Id the ID of the first user
     * @param user2Id the ID of the second user
     * @return true if the users are already matched
     */
    boolean areUsersMatched(Long user1Id, Long user2Id);
}