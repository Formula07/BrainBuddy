package com.kk.brainbuddy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kk.brainbuddy.dto.SwipeRequestDTO;
import com.kk.brainbuddy.dto.SwipeResultDTO;
import com.kk.brainbuddy.dto.UserProfileDTO;
import com.kk.brainbuddy.entity.User;
import com.kk.brainbuddy.exception.DuplicateSwipeException;
import com.kk.brainbuddy.exception.UserNotFoundException;
import com.kk.brainbuddy.service.SwipeResult;
import com.kk.brainbuddy.service.SwipeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for SwipeController
 * Requirements: 1.1, 1.2, 2.1, 2.2, 2.5, 6.2
 */
@WebMvcTest(SwipeController.class)
class SwipeControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private SwipeService swipeService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private User testUser1;
    private User testUser2;
    private SwipeRequestDTO validSwipeRequest;
    
    @BeforeEach
    void setUp() {
        testUser1 = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .bio("Computer Science student")
                .build();
        
        testUser2 = User.builder()
                .id(2L)
                .name("Jane Smith")
                .email("jane@example.com")
                .bio("Mathematics student")
                .build();
        
        validSwipeRequest = SwipeRequestDTO.builder()
                .swiperId(1L)
                .targetId(2L)
                .liked(true)
                .build();
    }
    
    @Test
    @WithMockUser
    void getNextPotentialMatch_ShouldReturnUserProfile_WhenMatchExists() throws Exception {
        // Given
        when(swipeService.getNextPotentialMatch(1L)).thenReturn(Optional.of(testUser2));
        
        // When & Then
        mockMvc.perform(get("/api/swipes/potential/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.name").value("Jane Smith"))
                .andExpect(jsonPath("$.bio").value("Mathematics student"));
    }
    
    @Test
    @WithMockUser
    void getNextPotentialMatch_ShouldReturnNotFound_WhenNoMatchExists() throws Exception {
        // Given
        when(swipeService.getNextPotentialMatch(1L)).thenReturn(Optional.empty());
        
        // When & Then
        mockMvc.perform(get("/api/swipes/potential/1"))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @WithMockUser
    void getNextPotentialMatch_ShouldReturnNotFound_WhenUserNotExists() throws Exception {
        // Given
        when(swipeService.getNextPotentialMatch(999L))
                .thenThrow(new UserNotFoundException("User not found with ID: 999"));
        
        // When & Then
        mockMvc.perform(get("/api/swipes/potential/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("User not found with ID: 999"));
    }
    
    @Test
    @WithMockUser
    void recordSwipe_ShouldReturnSuccess_WhenSwipeIsValid() throws Exception {
        // Given
        SwipeResult swipeResult = SwipeResult.builder()
                .success(true)
                .isMatch(false)
                .nextPotentialMatch(testUser2)
                .message("Swipe recorded successfully")
                .build();
        
        when(swipeService.recordSwipe(1L, 2L, true)).thenReturn(swipeResult);
        
        // When & Then
        mockMvc.perform(post("/api/swipes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validSwipeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.isMatch").value(false))
                .andExpect(jsonPath("$.message").value("Swipe recorded successfully"))
                .andExpect(jsonPath("$.nextPotentialMatch.id").value(2L))
                .andExpect(jsonPath("$.nextPotentialMatch.name").value("Jane Smith"));
    }
    
    @Test
    @WithMockUser
    void recordSwipe_ShouldReturnMatch_WhenMutualLikeExists() throws Exception {
        // Given
        SwipeResult swipeResult = SwipeResult.builder()
                .success(true)
                .isMatch(true)
                .nextPotentialMatch(null)
                .message("It's a match!")
                .build();
        
        when(swipeService.recordSwipe(1L, 2L, true)).thenReturn(swipeResult);
        
        // When & Then
        mockMvc.perform(post("/api/swipes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validSwipeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.isMatch").value(true))
                .andExpect(jsonPath("$.message").value("It's a match!"))
                .andExpect(jsonPath("$.nextPotentialMatch").isEmpty());
    }
    
    @Test
    @WithMockUser
    void recordSwipe_ShouldReturnBadRequest_WhenSwipeRequestIsInvalid() throws Exception {
        // Given
        SwipeRequestDTO invalidRequest = SwipeRequestDTO.builder()
                .swiperId(null) // Invalid - null swiper ID
                .targetId(2L)
                .liked(true)
                .build();
        
        // When & Then
        mockMvc.perform(post("/api/swipes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }
    
    @Test
    @WithMockUser
    void recordSwipe_ShouldReturnConflict_WhenDuplicateSwipeAttempted() throws Exception {
        // Given
        when(swipeService.recordSwipe(1L, 2L, true))
                .thenThrow(new DuplicateSwipeException("User 1 has already swiped on user 2"));
        
        // When & Then
        mockMvc.perform(post("/api/swipes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validSwipeRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("DUPLICATE_SWIPE"))
                .andExpect(jsonPath("$.message").value("User 1 has already swiped on user 2"));
    }
    
    @Test
    @WithMockUser
    void recordSwipe_ShouldReturnNotFound_WhenUserNotExists() throws Exception {
        // Given
        when(swipeService.recordSwipe(999L, 2L, true))
                .thenThrow(new UserNotFoundException("User not found with ID: 999"));
        
        SwipeRequestDTO requestWithInvalidUser = SwipeRequestDTO.builder()
                .swiperId(999L)
                .targetId(2L)
                .liked(true)
                .build();
        
        // When & Then
        mockMvc.perform(post("/api/swipes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithInvalidUser)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("User not found with ID: 999"));
    }
    
    @Test
    @WithMockUser
    void recordSwipe_ShouldReturnBadRequest_WhenSwipeOperationFails() throws Exception {
        // Given
        SwipeResult failedResult = SwipeResult.builder()
                .success(false)
                .isMatch(false)
                .nextPotentialMatch(null)
                .message("Failed to record swipe")
                .build();
        
        when(swipeService.recordSwipe(1L, 2L, true)).thenReturn(failedResult);
        
        // When & Then
        mockMvc.perform(post("/api/swipes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validSwipeRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Failed to record swipe"));
    }
    
    @Test
    @WithMockUser
    void recordSwipe_ShouldValidateAllRequiredFields() throws Exception {
        // Given - Request with all null fields
        SwipeRequestDTO invalidRequest = SwipeRequestDTO.builder()
                .swiperId(null)
                .targetId(null)
                .liked(null)
                .build();
        
        // When & Then
        mockMvc.perform(post("/api/swipes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }
    
    @Test
    @WithMockUser
    void recordSwipe_ShouldHandleDislikeSwipe() throws Exception {
        // Given
        SwipeRequestDTO dislikeRequest = SwipeRequestDTO.builder()
                .swiperId(1L)
                .targetId(2L)
                .liked(false) // Dislike
                .build();
        
        SwipeResult swipeResult = SwipeResult.builder()
                .success(true)
                .isMatch(false)
                .nextPotentialMatch(testUser2)
                .message("Swipe recorded successfully")
                .build();
        
        when(swipeService.recordSwipe(1L, 2L, false)).thenReturn(swipeResult);
        
        // When & Then
        mockMvc.perform(post("/api/swipes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dislikeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.isMatch").value(false));
    }
}