package com.kk.brainbuddy.dto;

/**
 * DTO for authentication response
 */
public class AuthResponseDTO {
    
    private boolean success;
    private String message;
    private UserProfileDTO user;
    private String token; // For future JWT implementation

    // Constructors
    public AuthResponseDTO() {}

    public AuthResponseDTO(boolean success, String message, UserProfileDTO user, String token) {
        this.success = success;
        this.message = message;
        this.user = user;
        this.token = token;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UserProfileDTO getUser() {
        return user;
    }

    public void setUser(UserProfileDTO user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    // Builder pattern
    public static AuthResponseDTOBuilder builder() {
        return new AuthResponseDTOBuilder();
    }

    public static class AuthResponseDTOBuilder {
        private boolean success;
        private String message;
        private UserProfileDTO user;
        private String token;

        public AuthResponseDTOBuilder success(boolean success) {
            this.success = success;
            return this;
        }

        public AuthResponseDTOBuilder message(String message) {
            this.message = message;
            return this;
        }

        public AuthResponseDTOBuilder user(UserProfileDTO user) {
            this.user = user;
            return this;
        }

        public AuthResponseDTOBuilder token(String token) {
            this.token = token;
            return this;
        }

        public AuthResponseDTO build() {
            return new AuthResponseDTO(success, message, user, token);
        }
    }
}