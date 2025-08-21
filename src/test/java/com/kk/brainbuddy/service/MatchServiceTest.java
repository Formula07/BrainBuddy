package com.kk.brainbuddy.service;

import com.kk.brainbuddy.entity.Match;
import com.kk.brainbuddy.entity.Swipe;
import com.kk.brainbuddy.entity.User;
import com.kk.brainbuddy.exception.UserNotFoundException;
import com.kk.brainbuddy.repository.MatchRepository;
import com.kk.brainbuddy.repository.SwipeRepository;
import com.kk.brainbuddy.repository.UserRepository;
import com.kk.brainbuddy.service.impl.MatchServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MatchService implementation
 * Requirements: 3.1, 3.3, 3.4, 4.1, 4.3
 */
@ExtendWith(MockitoExtension.class)
class MatchServiceTest {
    
    @Mock
    private MatchRepository matchRepository;
    
    @Mock
    private SwipeRepository swipeRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private MatchServiceImpl matchService;
    
    private User testUser1;
    private User testUser2;
    private Match testMatch;
    private Swipe swipe1;
    private Swipe swipe2;
    
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
        
        testMatch = Match.builder()
                .id(1L)
                .user1(testUser1)
                .user2(testUser2)
                .build();
        
        swipe1 = Swipe.builder()
                .id(1L)
                .swiper(testUser1)
                .target(testUser2)
                .liked(true)
                .build();
        
        swipe2 = Swipe.builder()
                .id(2L)
                .swiper(testUser2)
                .target(testUser1)
                .liked(true)
                .build();
    }
    
    @Test
    void createMatchIfMutual_ShouldCreateMatch_WhenMutualLikesExist() {
        // Given
        Long user1Id = 1L;
        Long user2Id = 2L;
        
        when(matchRepository.areUsersMatched(user1Id, user2Id)).thenReturn(false);
        when(swipeRepository.findBySwiper_IdAndTarget_IdAndLiked(user1Id, user2Id, true))
                .thenReturn(Optional.of(swipe1));
        when(swipeRepository.findBySwiper_IdAndTarget_IdAndLiked(user2Id, user1Id, true))
                .thenReturn(Optional.of(swipe2));
        when(userRepository.findById(user1Id)).thenReturn(Optional.of(testUser1));
        when(userRepository.findById(user2Id)).thenReturn(Optional.of(testUser2));
        when(matchRepository.save(any(Match.class))).thenReturn(testMatch);
        
        // When
        Optional<Match> result = matchService.createMatchIfMutual(user1Id, user2Id);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(testMatch.getId(), result.get().getId());
        verify(matchRepository).save(any(Match.class));
    }
    
    @Test
    void createMatchIfMutual_ShouldReturnEmpty_WhenUsersAlreadyMatched() {
        // Given
        Long user1Id = 1L;
        Long user2Id = 2L;
        
        when(matchRepository.areUsersMatched(user1Id, user2Id)).thenReturn(true);
        
        // When
        Optional<Match> result = matchService.createMatchIfMutual(user1Id, user2Id);
        
        // Then
        assertFalse(result.isPresent());
        verify(matchRepository, never()).save(any(Match.class));
    }
    
    @Test
    void createMatchIfMutual_ShouldReturnEmpty_WhenOnlyOneUserLikes() {
        // Given
        Long user1Id = 1L;
        Long user2Id = 2L;
        
        when(matchRepository.areUsersMatched(user1Id, user2Id)).thenReturn(false);
        when(swipeRepository.findBySwiper_IdAndTarget_IdAndLiked(user1Id, user2Id, true))
                .thenReturn(Optional.of(swipe1));
        when(swipeRepository.findBySwiper_IdAndTarget_IdAndLiked(user2Id, user1Id, true))
                .thenReturn(Optional.empty());
        
        // When
        Optional<Match> result = matchService.createMatchIfMutual(user1Id, user2Id);
        
        // Then
        assertFalse(result.isPresent());
        verify(matchRepository, never()).save(any(Match.class));
    }
    
    @Test
    void createMatchIfMutual_ShouldReturnEmpty_WhenNoLikesExist() {
        // Given
        Long user1Id = 1L;
        Long user2Id = 2L;
        
        when(matchRepository.areUsersMatched(user1Id, user2Id)).thenReturn(false);
        when(swipeRepository.findBySwiper_IdAndTarget_IdAndLiked(user1Id, user2Id, true))
                .thenReturn(Optional.empty());
        when(swipeRepository.findBySwiper_IdAndTarget_IdAndLiked(user2Id, user1Id, true))
                .thenReturn(Optional.empty());
        
        // When
        Optional<Match> result = matchService.createMatchIfMutual(user1Id, user2Id);
        
        // Then
        assertFalse(result.isPresent());
        verify(matchRepository, never()).save(any(Match.class));
    }
    
    @Test
    void createMatchIfMutual_ShouldThrowException_WhenUser1NotFound() {
        // Given
        Long user1Id = 999L;
        Long user2Id = 2L;
        
        when(matchRepository.areUsersMatched(user1Id, user2Id)).thenReturn(false);
        when(swipeRepository.findBySwiper_IdAndTarget_IdAndLiked(user1Id, user2Id, true))
                .thenReturn(Optional.of(swipe1));
        when(swipeRepository.findBySwiper_IdAndTarget_IdAndLiked(user2Id, user1Id, true))
                .thenReturn(Optional.of(swipe2));
        when(userRepository.findById(user1Id)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(UserNotFoundException.class, 
                () -> matchService.createMatchIfMutual(user1Id, user2Id));
    }
    
    @Test
    void createMatchIfMutual_ShouldThrowException_WhenUser2NotFound() {
        // Given
        Long user1Id = 1L;
        Long user2Id = 999L;
        
        when(matchRepository.areUsersMatched(user1Id, user2Id)).thenReturn(false);
        when(swipeRepository.findBySwiper_IdAndTarget_IdAndLiked(user1Id, user2Id, true))
                .thenReturn(Optional.of(swipe1));
        when(swipeRepository.findBySwiper_IdAndTarget_IdAndLiked(user2Id, user1Id, true))
                .thenReturn(Optional.of(swipe2));
        when(userRepository.findById(user1Id)).thenReturn(Optional.of(testUser1));
        when(userRepository.findById(user2Id)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(UserNotFoundException.class, 
                () -> matchService.createMatchIfMutual(user1Id, user2Id));
    }
    
    @Test
    void getUserMatches_ShouldReturnMatches_WhenUserExists() {
        // Given
        Long userId = 1L;
        List<Match> expectedMatches = Arrays.asList(testMatch);
        
        when(userRepository.existsById(userId)).thenReturn(true);
        when(matchRepository.findMatchesByUserId(userId)).thenReturn(expectedMatches);
        
        // When
        List<Match> result = matchService.getUserMatches(userId);
        
        // Then
        assertEquals(expectedMatches.size(), result.size());
        assertEquals(expectedMatches.get(0).getId(), result.get(0).getId());
    }
    
    @Test
    void getUserMatches_ShouldReturnEmptyList_WhenNoMatches() {
        // Given
        Long userId = 1L;
        
        when(userRepository.existsById(userId)).thenReturn(true);
        when(matchRepository.findMatchesByUserId(userId)).thenReturn(Collections.emptyList());
        
        // When
        List<Match> result = matchService.getUserMatches(userId);
        
        // Then
        assertTrue(result.isEmpty());
    }
    
    @Test
    void getUserMatches_ShouldThrowException_WhenUserNotFound() {
        // Given
        Long userId = 999L;
        
        when(userRepository.existsById(userId)).thenReturn(false);
        
        // When & Then
        assertThrows(UserNotFoundException.class, 
                () -> matchService.getUserMatches(userId));
    }
    
    @Test
    void areUsersMatched_ShouldReturnTrue_WhenUsersAreMatched() {
        // Given
        Long user1Id = 1L;
        Long user2Id = 2L;
        
        when(matchRepository.areUsersMatched(user1Id, user2Id)).thenReturn(true);
        
        // When
        boolean result = matchService.areUsersMatched(user1Id, user2Id);
        
        // Then
        assertTrue(result);
    }
    
    @Test
    void areUsersMatched_ShouldReturnFalse_WhenUsersAreNotMatched() {
        // Given
        Long user1Id = 1L;
        Long user2Id = 2L;
        
        when(matchRepository.areUsersMatched(user1Id, user2Id)).thenReturn(false);
        
        // When
        boolean result = matchService.areUsersMatched(user1Id, user2Id);
        
        // Then
        assertFalse(result);
    }
}