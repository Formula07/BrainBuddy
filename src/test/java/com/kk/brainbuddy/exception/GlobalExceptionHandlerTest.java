package com.kk.brainbuddy.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GlobalExceptionHandler
 * Requirements: 6.2
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        // Setup is handled by @InjectMocks
    }

    @Test
    void handleUserNotFound_ShouldReturnNotFoundResponse() {
        // Given
        String errorMessage = "User not found with ID: 123";
        UserNotFoundException exception = new UserNotFoundException(errorMessage);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleUserNotFound(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals("USER_NOT_FOUND", errorResponse.getError());
        assertEquals(errorMessage, errorResponse.getMessage());
        assertEquals(404, errorResponse.getStatus());
        assertNotNull(errorResponse.getTimestamp());
        assertTrue(errorResponse.getTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void handleUserNotFound_WithUserIdConstructor_ShouldReturnNotFoundResponse() {
        // Given
        Long userId = 123L;
        UserNotFoundException exception = new UserNotFoundException(userId);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleUserNotFound(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals("USER_NOT_FOUND", errorResponse.getError());
        assertEquals("User not found with ID: 123", errorResponse.getMessage());
        assertEquals(404, errorResponse.getStatus());
    }

    @Test
    void handleDuplicateSwipe_ShouldReturnConflictResponse() {
        // Given
        String errorMessage = "User 1 has already swiped on user 2";
        DuplicateSwipeException exception = new DuplicateSwipeException(errorMessage);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleDuplicateSwipe(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals("DUPLICATE_SWIPE", errorResponse.getError());
        assertEquals(errorMessage, errorResponse.getMessage());
        assertEquals(409, errorResponse.getStatus());
        assertNotNull(errorResponse.getTimestamp());
        assertTrue(errorResponse.getTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void handleDuplicateSwipe_WithUserIdsConstructor_ShouldReturnConflictResponse() {
        // Given
        Long swiperId = 1L;
        Long targetId = 2L;
        DuplicateSwipeException exception = new DuplicateSwipeException(swiperId, targetId);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleDuplicateSwipe(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals("DUPLICATE_SWIPE", errorResponse.getError());
        assertEquals("User 1 has already swiped on user 2", errorResponse.getMessage());
        assertEquals(409, errorResponse.getStatus());
    }

    @Test
    void handleValidationErrors_ShouldReturnBadRequestResponse() {
        // Given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        FieldError fieldError1 = new FieldError("swipeRequest", "swiperId", "must not be null");
        FieldError fieldError2 = new FieldError("swipeRequest", "targetId", "must be positive");
        
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError1, fieldError2));

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidationErrors(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals("VALIDATION_ERROR", errorResponse.getError());
        assertTrue(errorResponse.getMessage().contains("Invalid request data"));
        assertTrue(errorResponse.getMessage().contains("swiperId"));
        assertTrue(errorResponse.getMessage().contains("targetId"));
        assertEquals(400, errorResponse.getStatus());
        assertNotNull(errorResponse.getTimestamp());
    }

    @Test
    void handleMethodArgumentTypeMismatch_ShouldReturnBadRequestResponse() {
        // Given
        MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
        when(exception.getValue()).thenReturn("invalid_id");
        when(exception.getMessage()).thenReturn("Failed to convert value");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleMethodArgumentTypeMismatch(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals("INVALID_PARAMETER", errorResponse.getError());
        assertEquals("Invalid parameter value: invalid_id", errorResponse.getMessage());
        assertEquals(400, errorResponse.getStatus());
        assertNotNull(errorResponse.getTimestamp());
    }

    @Test
    void handleRuntimeException_ShouldReturnInternalServerErrorResponse() {
        // Given
        RuntimeException exception = new RuntimeException("Database connection failed");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleRuntimeException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals("INTERNAL_ERROR", errorResponse.getError());
        assertEquals("An internal error occurred", errorResponse.getMessage());
        assertEquals(500, errorResponse.getStatus());
        assertNotNull(errorResponse.getTimestamp());
    }

    @Test
    void handleGeneralException_ShouldReturnInternalServerErrorResponse() {
        // Given
        Exception exception = new Exception("Unexpected error occurred");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGeneralException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals("UNEXPECTED_ERROR", errorResponse.getError());
        assertEquals("An unexpected error occurred", errorResponse.getMessage());
        assertEquals(500, errorResponse.getStatus());
        assertNotNull(errorResponse.getTimestamp());
    }

    @Test
    void handleRuntimeException_ShouldNotExposeInternalDetails() {
        // Given
        RuntimeException exception = new RuntimeException("Sensitive database connection string: jdbc://secret");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleRuntimeException(exception);

        // Then
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        // Verify that sensitive information is not exposed
        assertFalse(errorResponse.getMessage().contains("jdbc://secret"));
        assertFalse(errorResponse.getMessage().contains("database connection string"));
        assertEquals("An internal error occurred", errorResponse.getMessage());
    }

    @Test
    void handleGeneralException_ShouldNotExposeInternalDetails() {
        // Given
        Exception exception = new Exception("Internal system error with sensitive data: API_KEY_12345");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGeneralException(exception);

        // Then
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        // Verify that sensitive information is not exposed
        assertFalse(errorResponse.getMessage().contains("API_KEY_12345"));
        assertFalse(errorResponse.getMessage().contains("sensitive data"));
        assertEquals("An unexpected error occurred", errorResponse.getMessage());
    }

    @Test
    void allExceptionHandlers_ShouldSetTimestampWithinReasonableTime() {
        // Given
        LocalDateTime beforeCall = LocalDateTime.now();
        UserNotFoundException userException = new UserNotFoundException("Test user not found");
        DuplicateSwipeException swipeException = new DuplicateSwipeException("Test duplicate swipe");
        RuntimeException runtimeException = new RuntimeException("Test runtime error");

        // When
        ResponseEntity<ErrorResponse> userResponse = globalExceptionHandler.handleUserNotFound(userException);
        ResponseEntity<ErrorResponse> swipeResponse = globalExceptionHandler.handleDuplicateSwipe(swipeException);
        ResponseEntity<ErrorResponse> runtimeResponse = globalExceptionHandler.handleRuntimeException(runtimeException);
        LocalDateTime afterCall = LocalDateTime.now();

        // Then
        assertNotNull(userResponse.getBody().getTimestamp());
        assertNotNull(swipeResponse.getBody().getTimestamp());
        assertNotNull(runtimeResponse.getBody().getTimestamp());

        assertTrue(userResponse.getBody().getTimestamp().isAfter(beforeCall.minusSeconds(1)));
        assertTrue(userResponse.getBody().getTimestamp().isBefore(afterCall.plusSeconds(1)));
        
        assertTrue(swipeResponse.getBody().getTimestamp().isAfter(beforeCall.minusSeconds(1)));
        assertTrue(swipeResponse.getBody().getTimestamp().isBefore(afterCall.plusSeconds(1)));
        
        assertTrue(runtimeResponse.getBody().getTimestamp().isAfter(beforeCall.minusSeconds(1)));
        assertTrue(runtimeResponse.getBody().getTimestamp().isBefore(afterCall.plusSeconds(1)));
    }
}