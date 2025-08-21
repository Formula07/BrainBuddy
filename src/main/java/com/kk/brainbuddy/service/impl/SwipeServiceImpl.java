package com.kk.brainbuddy.service.impl;

import com.kk.brainbuddy.entity.Swipe;
import com.kk.brainbuddy.entity.User;
import com.kk.brainbuddy.exception.DuplicateSwipeException;
import com.kk.brainbuddy.exception.UserNotFoundException;
import com.kk.brainbuddy.repository.SwipeRepository;
import com.kk.brainbuddy.repository.UserRepository;
import com.kk.brainbuddy.service.MatchService;
import com.kk.brainbuddy.service.SwipeResult;
import com.kk.brainbuddy.service.SwipeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of SwipeService for handling swipe operations
 * Requirements: 1.1, 1.3, 2.1, 2.2, 2.4, 5.1, 5.3
 */
@Service
public class SwipeServiceImpl implements SwipeService {
    
    private static final Logger log = LoggerFactory.getLogger(SwipeServiceImpl.class);
    
    private final SwipeRepository swipeRepository;
    private final UserRepository userRepository;
    private final MatchService matchService;

    public SwipeServiceImpl(SwipeRepository swipeRepository, UserRepository userRepository, MatchService matchService) {
        this.swipeRepository = swipeRepository;
        this.userRepository = userRepository;
        this.matchService = matchService;
    }
    
    /**
     * Get the next potential match for a user
     * Requirements: 1.1, 5.1
     */
    @Override
    public Optional<User> getNextPotentialMatch(Long userId) {
        log.debug("Finding next potential match for user: {}", userId);
        
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        
        // Get potential matches (users not swiped on yet)
        Pageable pageable = PageRequest.of(0, 1);
        List<User> potentialMatches = swipeRepository.findPotentialMatches(userId, pageable);
        
        if (potentialMatches.isEmpty()) {
            log.debug("No potential matches found for user: {}", userId);
            return Optional.empty();
        }
        
        User nextMatch = potentialMatches.get(0);
        log.debug("Found potential match for user {}: {}", userId, nextMatch.getId());
        return Optional.of(nextMatch);
    }
    
    /**
     * Record a swipe action and check for mutual matches
     * Requirements: 2.1, 2.2, 2.4, 5.3
     */
    @Override
    @Transactional
    public SwipeResult recordSwipe(Long swiperId, Long targetId, boolean liked) {
        log.debug("Recording swipe: swiper={}, target={}, liked={}", swiperId, targetId, liked);
        
        // Validate input
        validateSwipeInput(swiperId, targetId);
        
        // Check if user has already swiped on target
        if (hasUserSwipedOnTarget(swiperId, targetId)) {
            throw new DuplicateSwipeException(swiperId, targetId);
        }
        
        // Get users to ensure they exist
        User swiper = userRepository.findById(swiperId)
                .orElseThrow(() -> new UserNotFoundException(swiperId));
        User target = userRepository.findById(targetId)
                .orElseThrow(() -> new UserNotFoundException(targetId));
        
        // Create and save swipe
        Swipe swipe = Swipe.builder()
                .swiper(swiper)
                .target(target)
                .liked(liked)
                .build();
        
        swipeRepository.save(swipe);
        log.debug("Swipe recorded successfully");
        
        // Check for mutual match if this was a like
        boolean isMatch = false;
        if (liked) {
            Optional<com.kk.brainbuddy.entity.Match> match = matchService.createMatchIfMutual(swiperId, targetId);
            isMatch = match.isPresent();
            if (isMatch) {
                log.debug("Mutual match created between users {} and {}", swiperId, targetId);
            }
        }
        
        // Get next potential match
        Optional<User> nextPotentialMatch = getNextPotentialMatch(swiperId);
        
        // Build result
        SwipeResult.SwipeResultBuilder resultBuilder = SwipeResult.builder()
                .success(true)
                .isMatch(isMatch);
        
        if (nextPotentialMatch.isPresent()) {
            resultBuilder.nextPotentialMatch(nextPotentialMatch.get())
                    .message("Swipe recorded successfully");
        } else {
            resultBuilder.message("Swipe recorded successfully. No more potential matches available");
        }
        
        return resultBuilder.build();
    }
    
    /**
     * Check if a user has already swiped on a target user
     * Requirements: 2.3, 5.1
     */
    @Override
    public boolean hasUserSwipedOnTarget(Long swiperId, Long targetId) {
        return swipeRepository.existsBySwiper_IdAndTarget_Id(swiperId, targetId);
    }
    
    /**
     * Validate swipe input parameters
     */
    private void validateSwipeInput(Long swiperId, Long targetId) {
        if (swiperId == null) {
            throw new IllegalArgumentException("Swiper ID cannot be null");
        }
        if (targetId == null) {
            throw new IllegalArgumentException("Target ID cannot be null");
        }
        if (swiperId.equals(targetId)) {
            throw new IllegalArgumentException("User cannot swipe on themselves");
        }
    }
}