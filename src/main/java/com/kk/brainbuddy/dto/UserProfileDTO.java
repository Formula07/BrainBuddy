package com.kk.brainbuddy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for user profile data transfer
 * Requirements: 1.2, 4.2
 */
public class UserProfileDTO {
    
    @NotNull(message = "User ID is required")
    private Long id;
    
    @NotBlank(message = "Name is required")
    private String name;
    
    private String bio;

    // Constructors
    public UserProfileDTO() {}

    public UserProfileDTO(Long id, String name, String bio) {
        this.id = id;
        this.name = name;
        this.bio = bio;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    // Builder pattern
    public static UserProfileDTOBuilder builder() {
        return new UserProfileDTOBuilder();
    }

    public static class UserProfileDTOBuilder {
        private Long id;
        private String name;
        private String bio;

        public UserProfileDTOBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public UserProfileDTOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public UserProfileDTOBuilder bio(String bio) {
            this.bio = bio;
            return this;
        }

        public UserProfileDTO build() {
            return new UserProfileDTO(id, name, bio);
        }
    }
}