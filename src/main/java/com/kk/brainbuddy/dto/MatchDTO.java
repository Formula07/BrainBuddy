package com.kk.brainbuddy.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * DTO for match information transfer
 * Requirements: 3.4, 4.2, 4.3
 */
public class MatchDTO {
    
    @NotNull(message = "Match ID is required")
    private Long matchId;
    
    @NotNull(message = "Matched user is required")
    private UserProfileDTO matchedUser;
    
    private LocalDateTime matchedAt;

    // Constructors
    public MatchDTO() {}

    public MatchDTO(Long matchId, UserProfileDTO matchedUser, LocalDateTime matchedAt) {
        this.matchId = matchId;
        this.matchedUser = matchedUser;
        this.matchedAt = matchedAt;
    }

    // Getters and Setters
    public Long getMatchId() {
        return matchId;
    }

    public void setMatchId(Long matchId) {
        this.matchId = matchId;
    }

    public UserProfileDTO getMatchedUser() {
        return matchedUser;
    }

    public void setMatchedUser(UserProfileDTO matchedUser) {
        this.matchedUser = matchedUser;
    }

    public LocalDateTime getMatchedAt() {
        return matchedAt;
    }

    public void setMatchedAt(LocalDateTime matchedAt) {
        this.matchedAt = matchedAt;
    }

    // Builder pattern
    public static MatchDTOBuilder builder() {
        return new MatchDTOBuilder();
    }

    public static class MatchDTOBuilder {
        private Long matchId;
        private UserProfileDTO matchedUser;
        private LocalDateTime matchedAt;

        public MatchDTOBuilder matchId(Long matchId) {
            this.matchId = matchId;
            return this;
        }

        public MatchDTOBuilder matchedUser(UserProfileDTO matchedUser) {
            this.matchedUser = matchedUser;
            return this;
        }

        public MatchDTOBuilder matchedAt(LocalDateTime matchedAt) {
            this.matchedAt = matchedAt;
            return this;
        }

        public MatchDTO build() {
            return new MatchDTO(matchId, matchedUser, matchedAt);
        }
    }
}