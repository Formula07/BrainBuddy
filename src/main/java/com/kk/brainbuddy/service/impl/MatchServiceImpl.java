package com.kk.brainbuddy.service.impl;

import com.kk.brainbuddy.entity.Match;
import com.kk.brainbuddy.entity.User;
import com.kk.brainbuddy.exception.UserNotFoundException;
import com.kk.brainbuddy.repository.MatchRepository;
import com.kk.brainbuddy.repository.SwipeRepository;
import com.kk.brainbuddy.repository.UserRepository;
import com.kk.brainbuddy.service.MatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of MatchService for handling match operations
 * Requirements: 3.1, 3.3, 3.4, 4.1, 4.3
 */
@Service
public class MatchServiceImpl implements MatchService {
    
    private static final Logger log = LoggerFactory.getLogger(MatchServiceImpl.class);
    
    private final MatchRepository matchRepository;
    private final SwipeRepository swipeRepository;
    private final UserRepository userRepository;

    public MatchServiceImpl(MatchRepository matchRepository, SwipeRepository swipeRepository, UserRepository userRepository) {
        this.matchRepository = matchRepository;
        this.swipeRepository = swipeRepository;
        this.userRepository = userRepository;
    }
    
    /**
     * Create a match if two users have mutually liked each other
     * Requirements: 3.1, 3.3
     */
    @Override
    @Transactional
    public Optional<Match> createMatchIfMutual(Long user1Id, Long user2Id) {
        log.debug("Checking for mutual match between users {} and {}", user1Id, user2Id);
        
        // Check if users are already matched
        if (areUsersMatched(user1Id, user2Id)) {
            log.debug("Users {} and {} are already matched", user1Id, user2Id);
            return Optional.empty();
        }
        
        // Check if both users have liked each other
        boolean user1LikesUser2 = swipeRepository.findBySwiper_IdAndTarget_IdAndLiked(user1Id, user2Id, true).isPresent();
        boolean user2LikesUser1 = swipeRepository.findBySwiper_IdAndTarget_IdAndLiked(user2Id, user1Id, true).isPresent();
        
        if (user1LikesUser2 && user2LikesUser1) {
            // Get users to ensure they exist
            User user1 = userRepository.findById(user1Id)
                    .orElseThrow(() -> new UserNotFoundException(user1Id));
            User user2 = userRepository.findById(user2Id)
                    .orElseThrow(() -> new UserNotFoundException(user2Id));
            
            // Create match
            Match match = Match.builder()
                    .user1(user1)
                    .user2(user2)
                    .build();
            
            Match savedMatch = matchRepository.save(match);
            log.debug("Created mutual match between users {} and {}", user1Id, user2Id);
            return Optional.of(savedMatch);
        }
        
        log.debug("No mutual match found between users {} and {}", user1Id, user2Id);
        return Optional.empty();
    }
    
    /**
     * Get all matches for a user
     * Requirements: 4.1
     */
    @Override
    public List<Match> getUserMatches(Long userId) {
        log.debug("Getting matches for user: {}", userId);
        
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        
        return matchRepository.findMatchesByUserId(userId);
    }
    
    /**
     * Check if two users are already matched
     * Requirements: 3.3, 4.3
     */
    @Override
    public boolean areUsersMatched(Long user1Id, Long user2Id) {
        return matchRepository.areUsersMatched(user1Id, user2Id);
    }
}