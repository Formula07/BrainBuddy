package com.kk.brainbuddy.exception;

/**
 * Exception thrown when attempting to swipe on the same user twice
 */
public class DuplicateSwipeException extends RuntimeException {
    
    public DuplicateSwipeException(String message) {
        super(message);
    }
    
    public DuplicateSwipeException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public DuplicateSwipeException(Long swiperId, Long targetId) {
        super("User " + swiperId + " has already swiped on user " + targetId);
    }
}