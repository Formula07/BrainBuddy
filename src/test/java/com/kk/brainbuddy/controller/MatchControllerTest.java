package com.kk.brainbuddy.controller;

import com.kk.brainbuddy.entity.Match;
import com.kk.brainbuddy.entity.User;
import com.kk.brainbuddy.exception.UserNotFoundException;
import com.kk.brainbuddy.service.MatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for MatchController
 * Requirements: 4.1, 4.2, 4.4, 6.2
 */
@WebMvcTest(MatchController.class)
class MatchControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private MatchService matchService;
    
    private User testUser1;
    private User testUser2;
    private User testUser3;
    private Match testMatch1;
    private Match testMatch2;
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
        
        testUser3 = User.builder()
                .id(3L)
                .name("Bob Johnson")
                .email("bob@example.com")
                .bio("Physics student")
                .build();
        
        testMatch1 = Match.builder()
                .id(1L)
                .user1(testUser1)
                .user2(testUser2)
                .createdAt(LocalDateTime.of(2024, 1, 15, 10, 30))
                .build();
        
        testMatch2 = Match.builder()
                .id(2L)
                .user1(testUser3)
                .user2(testUser1)
                .createdAt(LocalDateTime.of(2024, 1, 16, 14, 45))
                .build();
    }
    
    @Test
    @WithMockUser
    void getUserMatches_ShouldReturnMatchList_WhenUserHasMatches() throws Exception {
        // Given
        List<Match> matches = Arrays.asList(testMatch1, testMatch2);
        when(matchService.getUserMatches(1L)).thenReturn(matches);
        
        // When & Then
        mockMvc.perform(get("/api/matches/user/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].matchId").value(1L))
                .andExpect(jsonPath("$[0].matchedUser.id").value(2L))
                .andExpect(jsonPath("$[0].matchedUser.name").value("Jane Smith"))
                .andExpect(jsonPath("$[0].matchedUser.bio").value("Mathematics student"))
                .andExpect(jsonPath("$[0].matchedAt").value("2024-01-15T10:30:00"))
                .andExpect(jsonPath("$[1].matchId").value(2L))
                .andExpect(jsonPath("$[1].matchedUser.id").value(3L))
                .andExpect(jsonPath("$[1].matchedUser.name").value("Bob Johnson"))
                .andExpect(jsonPath("$[1].matchedUser.bio").value("Physics student"))
                .andExpect(jsonPath("$[1].matchedAt").value("2024-01-16T14:45:00"));
    }
    
    @Test
    @WithMockUser
    void getUserMatches_ShouldReturnEmptyList_WhenUserHasNoMatches() throws Exception {
        // Given
        when(matchService.getUserMatches(1L)).thenReturn(Collections.emptyList());
        
        // When & Then
        mockMvc.perform(get("/api/matches/user/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));
    }
    
    @Test
    @WithMockUser
    void getUserMatches_ShouldReturnNotFound_WhenUserDoesNotExist() throws Exception {
        // Given
        when(matchService.getUserMatches(999L))
                .thenThrow(new UserNotFoundException("User not found with ID: 999"));
        
        // When & Then
        mockMvc.perform(get("/api/matches/user/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("User not found with ID: 999"))
                .andExpect(jsonPath("$.status").value(404));
    }
    
    @Test
    @WithMockUser
    void getUserMatches_ShouldReturnCorrectMatchedUser_WhenUserIsUser1() throws Exception {
        // Given - User 1 is requesting matches, should see User 2 as matched user
        List<Match> matches = Arrays.asList(testMatch1);
        when(matchService.getUserMatches(1L)).thenReturn(matches);
        
        // When & Then
        mockMvc.perform(get("/api/matches/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].matchedUser.id").value(2L))
                .andExpect(jsonPath("$[0].matchedUser.name").value("Jane Smith"));
    }
    
    @Test
    @WithMockUser
    void getUserMatches_ShouldReturnCorrectMatchedUser_WhenUserIsUser2() throws Exception {
        // Given - User 2 is requesting matches, should see User 1 as matched user
        List<Match> matches = Arrays.asList(testMatch1);
        when(matchService.getUserMatches(2L)).thenReturn(matches);
        
        // When & Then
        mockMvc.perform(get("/api/matches/user/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].matchedUser.id").value(1L))
                .andExpect(jsonPath("$[0].matchedUser.name").value("John Doe"));
    }
    
    @Test
    @WithMockUser
    void getUserMatches_ShouldHandleMultipleMatches_WithCorrectOrdering() throws Exception {
        // Given - Multiple matches for user 1
        List<Match> matches = Arrays.asList(testMatch2, testMatch1); // Service returns in order
        when(matchService.getUserMatches(1L)).thenReturn(matches);
        
        // When & Then
        mockMvc.perform(get("/api/matches/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].matchId").value(2L))
                .andExpect(jsonPath("$[0].matchedUser.id").value(3L))
                .andExpect(jsonPath("$[1].matchId").value(1L))
                .andExpect(jsonPath("$[1].matchedUser.id").value(2L));
    }
    
    @Test
    @WithMockUser
    void getUserMatches_ShouldHandleNullBio_InUserProfile() throws Exception {
        // Given - User with null bio
        User userWithNullBio = User.builder()
                .id(4L)
                .name("Alice Brown")
                .email("alice@example.com")
                .bio(null)
                .build();
        
        Match matchWithNullBio = Match.builder()
                .id(3L)
                .user1(testUser1)
                .user2(userWithNullBio)
                .createdAt(LocalDateTime.of(2024, 1, 17, 9, 15))
                .build();
        
        List<Match> matches = Arrays.asList(matchWithNullBio);
        when(matchService.getUserMatches(1L)).thenReturn(matches);
        
        // When & Then
        mockMvc.perform(get("/api/matches/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].matchedUser.id").value(4L))
                .andExpect(jsonPath("$[0].matchedUser.name").value("Alice Brown"))
                .andExpect(jsonPath("$[0].matchedUser.bio").isEmpty())
                .andExpect(jsonPath("$[0].matchedAt").value("2024-01-17T09:15:00"));
    }
    
    @Test
    @WithMockUser
    void getUserMatches_ShouldHandleInternalServerError_WhenServiceThrowsRuntimeException() throws Exception {
        // Given
        when(matchService.getUserMatches(anyLong()))
                .thenThrow(new RuntimeException("Database connection failed"));
        
        // When & Then
        mockMvc.perform(get("/api/matches/user/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("An internal error occurred"))
                .andExpect(jsonPath("$.status").value(500));
    }
    
    @Test
    @WithMockUser
    void getUserMatches_ShouldValidatePathVariable_WhenUserIdIsInvalid() throws Exception {
        // When & Then - Testing with non-numeric user ID
        mockMvc.perform(get("/api/matches/user/invalid"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @WithMockUser
    void getUserMatches_ShouldReturnProperContentType() throws Exception {
        // Given
        when(matchService.getUserMatches(1L)).thenReturn(Collections.emptyList());
        
        // When & Then
        mockMvc.perform(get("/api/matches/user/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string("Content-Type", "application/json"));
    }
    
    @Test
    @WithMockUser
    void getUserMatches_ShouldReturnMatchedAtTimestamp_WhenMatchHasCreatedAt() throws Exception {
        // Given - Match with specific timestamp
        LocalDateTime matchTime = LocalDateTime.of(2024, 2, 20, 16, 30, 45);
        Match matchWithTimestamp = Match.builder()
                .id(5L)
                .user1(testUser1)
                .user2(testUser2)
                .createdAt(matchTime)
                .build();
        
        List<Match> matches = Arrays.asList(matchWithTimestamp);
        when(matchService.getUserMatches(1L)).thenReturn(matches);
        
        // When & Then
        mockMvc.perform(get("/api/matches/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].matchId").value(5L))
                .andExpect(jsonPath("$[0].matchedAt").value("2024-02-20T16:30:45"))
                .andExpect(jsonPath("$[0].matchedUser.id").value(2L));
    }
}