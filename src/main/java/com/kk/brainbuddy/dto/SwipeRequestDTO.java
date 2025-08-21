package com.kk.brainbuddy.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO for handling swipe API requests
 * Requirements: 2.1, 2.2
 */
public class SwipeRequestDTO {
    
    @NotNull(message = "Swiper ID is required")
    private Long swiperId;
    
    @NotNull(message = "Target ID is required")
    private Long targetId;
    
    @NotNull(message = "Liked status is required")
    private Boolean liked;

    // Constructors
    public SwipeRequestDTO() {}

    public SwipeRequestDTO(Long swiperId, Long targetId, Boolean liked) {
        this.swiperId = swiperId;
        this.targetId = targetId;
        this.liked = liked;
    }

    // Getters and Setters
    public Long getSwiperId() {
        return swiperId;
    }

    public void setSwiperId(Long swiperId) {
        this.swiperId = swiperId;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public Boolean getLiked() {
        return liked;
    }

    public void setLiked(Boolean liked) {
        this.liked = liked;
    }

    // Builder pattern
    public static SwipeRequestDTOBuilder builder() {
        return new SwipeRequestDTOBuilder();
    }

    public static class SwipeRequestDTOBuilder {
        private Long swiperId;
        private Long targetId;
        private Boolean liked;

        public SwipeRequestDTOBuilder swiperId(Long swiperId) {
            this.swiperId = swiperId;
            return this;
        }

        public SwipeRequestDTOBuilder targetId(Long targetId) {
            this.targetId = targetId;
            return this;
        }

        public SwipeRequestDTOBuilder liked(Boolean liked) {
            this.liked = liked;
            return this;
        }

        public SwipeRequestDTO build() {
            return new SwipeRequestDTO(swiperId, targetId, liked);
        }
    }
}