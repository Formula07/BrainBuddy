package com.kk.brainbuddy.service;

import com.kk.brainbuddy.entity.Match;
import com.kk.brainbuddy.entity.Swipe;
import com.kk.brainbuddy.entity.User;
import com.kk.brainbuddy.exception.DuplicateSwipeException;
import com.kk.brainbuddy.exception.UserNotFoundException;
import com.kk.brainbuddy.repository.SwipeRepository;
import com.kk.brainbuddy.repository.UserRepository;
import com.kk.brainbuddy.service.impl.SwipeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SwipeService implementation
 * Requirements: 1.1, 1.3, 2.1, 2.2, 2.4, 5.1, 5.3
 */
@ExtendWith(MockitoExtension.class)
class SwipeServiceTest {
    
    @Mock
    private SwipeRepository swipeRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private MatchService matchService;
    
    @InjectMocks
    private SwipeServiceImpl swipeService;
    
    private User testUser1;
    private User testUser2;
    private User testUser3;
    
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
    }
    
    @Test
    void getNextPotentialMatch_ShouldReturnUser_WhenPotentialMatchExists() {
        // Given
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);
        when(swipeRepository.findPotentialMatches(eq(userId), any(Pageable.class)))
                .thenReturn(Arrays.asList(testUser2));
        
        // When
        Optional<User> result = swipeService.getNextPotentialMatch(userId);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(testUser2.getId(), result.get().getId());
        verify(swipeRepository).findPotentialMatches(eq(userId), any(PageRequest.class));
    }
    
    @Test
    void getNextPotentialMatch_ShouldReturnEmpty_WhenNoPotentialMatchExists() {
        // Given
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);
        when(swipeRepository.findPotentialMatches(eq(userId), any(Pageable.class)))
                .thenReturn(Collections.emptyList());
        
        // When
        Optional<User> result = swipeService.getNextPotentialMatch(userId);
        
        // Then
        assertFalse(result.isPresent());
    }
    
    @Test
    void getNextPotentialMatch_ShouldThrowException_WhenUserNotFound() {
        // Given
        Long userId = 999L;
        when(userRepository.existsById(userId)).thenReturn(false);
        
        // When & Then
        assertThrows(UserNotFoundException.class, 
                () -> swipeService.getNextPotentialMatch(userId));
    }
    
    @Test
    void recordSwipe_ShouldCreateSwipeAndReturnResult_WhenValidInput() {
        // Given
        Long swiperId = 1L;
        Long targetId = 2L;
        boolean liked = true;
        
        when(userRepository.findById(swiperId)).thenReturn(Optional.of(testUser1));
        when(userRepository.findById(targetId)).thenReturn(Optional.of(testUser2));
        when(swipeRepository.existsBySwiper_IdAndTarget_Id(swiperId, targetId)).thenReturn(false);
        when(matchService.createMatchIfMutual(swiperId, targetId)).thenReturn(Optional.empty());
        when(userRepository.existsById(swiperId)).thenReturn(true);
        when(swipeRepository.findPotentialMatches(eq(swiperId), any(Pageable.class)))
                .thenReturn(Arrays.asList(testUser3));
        
        // When
        SwipeResult result = swipeService.recordSwipe(swiperId, targetId, liked);
        
        // Then
        assertTrue(result.isSuccess());
        assertFalse(result.isMatch());
        assertNotNull(result.getNextPotentialMatch());
        assertEquals(testUser3.getId(), result.getNextPotentialMatch().getId());
        assertEquals("Swipe recorded successfully", result.getMessage());
        
        verify(swipeRepository).save(any(Swipe.class));
        verify(matchService).createMatchIfMutual(swiperId, targetId);
    }
    
    @Test
    void recordSwipe_ShouldCreateMatch_WhenMutualLikeExists() {
        // Given
        Long swiperId = 1L;
        Long targetId = 2L;
        boolean liked = true;
        Match mockMatch = Match.builder().id(1L).user1(testUser1).user2(testUser2).build();
        
        when(userRepository.findById(swiperId)).thenReturn(Optional.of(testUser1));
        when(userRepository.findById(targetId)).thenReturn(Optional.of(testUser2));
        when(swipeRepository.existsBySwiper_IdAndTarget_Id(swiperId, targetId)).thenReturn(false);
        when(matchService.createMatchIfMutual(swiperId, targetId)).thenReturn(Optional.of(mockMatch));
        when(userRepository.existsById(swiperId)).thenReturn(true);
        when(swipeRepository.findPotentialMatches(eq(swiperId), any(Pageable.class)))
                .thenReturn(Arrays.asList(testUser3));
        
        // When
        SwipeResult result = swipeService.recordSwipe(swiperId, targetId, liked);
        
        // Then
        assertTrue(result.isSuccess());
        assertTrue(result.isMatch());
        assertNotNull(result.getNextPotentialMatch());
        
        verify(swipeRepository).save(any(Swipe.class));
        verify(matchService).createMatchIfMutual(swiperId, targetId);
    }
    
    @Test
    void recordSwipe_ShouldNotCreateMatch_WhenSwipeIsDislike() {
        // Given
        Long swiperId = 1L;
        Long targetId = 2L;
        boolean liked = false;
        
        when(userRepository.findById(swiperId)).thenReturn(Optional.of(testUser1));
        when(userRepository.findById(targetId)).thenReturn(Optional.of(testUser2));
        when(swipeRepository.existsBySwiper_IdAndTarget_Id(swiperId, targetId)).thenReturn(false);
        when(userRepository.existsById(swiperId)).thenReturn(true);
        when(swipeRepository.findPotentialMatches(eq(swiperId), any(Pageable.class)))
                .thenReturn(Arrays.asList(testUser3));
        
        // When
        SwipeResult result = swipeService.recordSwipe(swiperId, targetId, liked);
        
        // Then
        assertTrue(result.isSuccess());
        assertFalse(result.isMatch());
        
        verify(swipeRepository).save(any(Swipe.class));
        verify(matchService, never()).createMatchIfMutual(anyLong(), anyLong());
    }
    
    @Test
    void recordSwipe_ShouldThrowException_WhenDuplicateSwipe() {
        // Given
        Long swiperId = 1L;
        Long targetId = 2L;
        boolean liked = true;
        
        when(swipeRepository.existsBySwiper_IdAndTarget_Id(swiperId, targetId)).thenReturn(true);
        
        // When & Then
        assertThrows(DuplicateSwipeException.class, 
                () -> swipeService.recordSwipe(swiperId, targetId, liked));
        
        verify(swipeRepository, never()).save(any(Swipe.class));
    }
    
    @Test
    void recordSwipe_ShouldThrowException_WhenSwiperNotFound() {
        // Given
        Long swiperId = 999L;
        Long targetId = 2L;
        boolean liked = true;
        
        when(swipeRepository.existsBySwiper_IdAndTarget_Id(swiperId, targetId)).thenReturn(false);
        when(userRepository.findById(swiperId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(UserNotFoundException.class, 
                () -> swipeService.recordSwipe(swiperId, targetId, liked));
    }
    
    @Test
    void recordSwipe_ShouldThrowException_WhenTargetNotFound() {
        // Given
        Long swiperId = 1L;
        Long targetId = 999L;
        boolean liked = true;
        
        when(swipeRepository.existsBySwiper_IdAndTarget_Id(swiperId, targetId)).thenReturn(false);
        when(userRepository.findById(swiperId)).thenReturn(Optional.of(testUser1));
        when(userRepository.findById(targetId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(UserNotFoundException.class, 
                () -> swipeService.recordSwipe(swiperId, targetId, liked));
    }
    
    @Test
    void recordSwipe_ShouldThrowException_WhenSwiperIdIsNull() {
        // Given
        Long swiperId = null;
        Long targetId = 2L;
        boolean liked = true;
        
        // When & Then
        assertThrows(IllegalArgumentException.class, 
                () -> swipeService.recordSwipe(swiperId, targetId, liked));
    }
    
    @Test
    void recordSwipe_ShouldThrowException_WhenTargetIdIsNull() {
        // Given
        Long swiperId = 1L;
        Long targetId = null;
        boolean liked = true;
        
        // When & Then
        assertThrows(IllegalArgumentException.class, 
                () -> swipeService.recordSwipe(swiperId, targetId, liked));
    }
    
    @Test
    void recordSwipe_ShouldThrowException_WhenUserSwipesOnThemselves() {
        // Given
        Long swiperId = 1L;
        Long targetId = 1L;
        boolean liked = true;
        
        // When & Then
        assertThrows(IllegalArgumentException.class, 
                () -> swipeService.recordSwipe(swiperId, targetId, liked));
    }
    
    @Test
    void recordSwipe_ShouldReturnMessageWhenNoMoreMatches() {
        // Given
        Long swiperId = 1L;
        Long targetId = 2L;
        boolean liked = true;
        
        when(userRepository.findById(swiperId)).thenReturn(Optional.of(testUser1));
        when(userRepository.findById(targetId)).thenReturn(Optional.of(testUser2));
        when(swipeRepository.existsBySwiper_IdAndTarget_Id(swiperId, targetId)).thenReturn(false);
        when(matchService.createMatchIfMutual(swiperId, targetId)).thenReturn(Optional.empty());
        when(userRepository.existsById(swiperId)).thenReturn(true);
        when(swipeRepository.findPotentialMatches(eq(swiperId), any(Pageable.class)))
                .thenReturn(Collections.emptyList());
        
        // When
        SwipeResult result = swipeService.recordSwipe(swiperId, targetId, liked);
        
        // Then
        assertTrue(result.isSuccess());
        assertNull(result.getNextPotentialMatch());
        assertEquals("Swipe recorded successfully. No more potential matches available", result.getMessage());
    }
    
    @Test
    void hasUserSwipedOnTarget_ShouldReturnTrue_WhenSwipeExists() {
        // Given
        Long swiperId = 1L;
        Long targetId = 2L;
        when(swipeRepository.existsBySwiper_IdAndTarget_Id(swiperId, targetId)).thenReturn(true);
        
        // When
        boolean result = swipeService.hasUserSwipedOnTarget(swiperId, targetId);
        
        // Then
        assertTrue(result);
    }
    
    @Test
    void hasUserSwipedOnTarget_ShouldReturnFalse_WhenSwipeDoesNotExist() {
        // Given
        Long swiperId = 1L;
        Long targetId = 2L;
        when(swipeRepository.existsBySwiper_IdAndTarget_Id(swiperId, targetId)).thenReturn(false);
        
        // When
        boolean result = swipeService.hasUserSwipedOnTarget(swiperId, targetId);
        
        // Then
        assertFalse(result);
    }
}