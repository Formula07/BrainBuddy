package com.kk.brainbuddy.service;

import com.kk.brainbuddy.entity.User;

/**
 * Result object for swipe operations
 */
public class SwipeResult {
    
    private boolean success;
    private boolean isMatch;
    private User nextPotentialMatch;
    private String message;

    // Constructors
    public SwipeResult() {}

    public SwipeResult(boolean success, boolean isMatch, User nextPotentialMatch, String message) {
        this.success = success;
        this.isMatch = isMatch;
        this.nextPotentialMatch = nextPotentialMatch;
        this.message = message;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isMatch() {
        return isMatch;
    }

    public void setMatch(boolean match) {
        isMatch = match;
    }

    public User getNextPotentialMatch() {
        return nextPotentialMatch;
    }

    public void setNextPotentialMatch(User nextPotentialMatch) {
        this.nextPotentialMatch = nextPotentialMatch;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // Builder pattern
    public static SwipeResultBuilder builder() {
        return new SwipeResultBuilder();
    }

    public static class SwipeResultBuilder {
        private boolean success;
        private boolean isMatch;
        private User nextPotentialMatch;
        private String message;

        public SwipeResultBuilder success(boolean success) {
            this.success = success;
            return this;
        }

        public SwipeResultBuilder isMatch(boolean isMatch) {
            this.isMatch = isMatch;
            return this;
        }

        public SwipeResultBuilder nextPotentialMatch(User nextPotentialMatch) {
            this.nextPotentialMatch = nextPotentialMatch;
            return this;
        }

        public SwipeResultBuilder message(String message) {
            this.message = message;
            return this;
        }

        public SwipeResult build() {
            return new SwipeResult(success, isMatch, nextPotentialMatch, message);
        }
    }
}