package com.kk.brainbuddy.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for swipe operation responses
 * Requirements: 2.4, 2.5, 3.1
 */
public class SwipeResultDTO {
    
    private boolean success;
    
    @JsonProperty("isMatch")
    private boolean isMatch;
    
    private UserProfileDTO nextPotentialMatch;
    
    private String message;

    // Constructors
    public SwipeResultDTO() {}

    public SwipeResultDTO(boolean success, boolean isMatch, UserProfileDTO nextPotentialMatch, String message) {
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

    public UserProfileDTO getNextPotentialMatch() {
        return nextPotentialMatch;
    }

    public void setNextPotentialMatch(UserProfileDTO nextPotentialMatch) {
        this.nextPotentialMatch = nextPotentialMatch;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // Builder pattern
    public static SwipeResultDTOBuilder builder() {
        return new SwipeResultDTOBuilder();
    }

    public static class SwipeResultDTOBuilder {
        private boolean success;
        private boolean isMatch;
        private UserProfileDTO nextPotentialMatch;
        private String message;

        public SwipeResultDTOBuilder success(boolean success) {
            this.success = success;
            return this;
        }

        public SwipeResultDTOBuilder isMatch(boolean isMatch) {
            this.isMatch = isMatch;
            return this;
        }

        public SwipeResultDTOBuilder nextPotentialMatch(UserProfileDTO nextPotentialMatch) {
            this.nextPotentialMatch = nextPotentialMatch;
            return this;
        }

        public SwipeResultDTOBuilder message(String message) {
            this.message = message;
            return this;
        }

        public SwipeResultDTO build() {
            return new SwipeResultDTO(success, isMatch, nextPotentialMatch, message);
        }
    }
}