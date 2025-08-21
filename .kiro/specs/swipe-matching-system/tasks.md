# Implementation Plan

- [x] 1. Create DTOs and request/response models





  - Create SwipeRequestDTO for handling swipe API requests
  - Create SwipeResultDTO for swipe operation responses
  - Create UserProfileDTO for user profile data transfer
  - Create MatchDTO for match information transfer
  - Add validation annotations to DTOs
  - _Requirements: 2.1, 2.2, 3.4, 4.2_

- [x] 2. Implement custom repository queries





  - Add custom query methods to SwipeRepository for finding potential matches
  - Add method to check if user has already swiped on target
  - Add query to find mutual likes between users
  - Add custom query methods to MatchRepository for user matches
  - Add method to check if users are already matched
  - Write unit tests for all repository methods
  - _Requirements: 1.1, 1.4, 2.3, 3.1, 4.1, 5.1, 5.2_

- [x] 3. Create SwipeService implementation





  - Implement getNextPotentialMatch method to find unswipped users
  - Implement recordSwipe method to save swipe actions
  - Add logic to prevent duplicate swipes on same user
  - Integrate with MatchService to check for mutual matches
  - Add proper error handling and validation
  - Write comprehensive unit tests for SwipeService
  - _Requirements: 1.1, 1.3, 2.1, 2.2, 2.4, 5.1, 5.3_

- [x] 4. Create MatchService implementation





  - Implement createMatchIfMutual method for automatic match creation
  - Implement getUserMatches method to retrieve user's matches
  - Add logic to prevent duplicate match creation
  - Add proper transaction handling for match creation
  - Write unit tests for MatchService methods
  - _Requirements: 3.1, 3.3, 3.4, 4.1, 4.3_

- [x] 5. Implement SwipeController REST endpoints





  - Create GET endpoint for retrieving next potential match
  - Create POST endpoint for recording swipe actions
  - Add proper request validation and error handling
  - Implement response mapping to DTOs
  - Add integration tests for SwipeController endpoints
  - _Requirements: 1.1, 1.2, 2.1, 2.2, 2.5, 6.2_

- [x] 6. Implement MatchController REST endpoints





  - Create GET endpoint for retrieving user matches
  - Add proper response formatting with MatchDTO
  - Implement error handling for invalid user IDs
  - Add integration tests for MatchController endpoints
  - _Requirements: 4.1, 4.2, 4.4, 6.2_

- [x] 7. Create global exception handling


  - Implement custom exception classes (UserNotFoundException, DuplicateSwipeException)
  - Create global exception handler with @ControllerAdvice
  - Add proper error response formatting
  - Ensure no technical details are exposed in error messages
  - ~~Write tests for exception handling scenarios~~ (Still needed)
  - _Requirements: 6.2_

- [x] 8. Create exception handling tests





  - Write unit tests for GlobalExceptionHandler
  - Test UserNotFoundException handling
  - Test DuplicateSwipeException handling
  - Test validation error handling
  - Test general exception scenarios
  - _Requirements: 6.2_

- [x] 10. Add database performance optimizations





  - Add database indexes for swiper_id and target_id in swipes table
  - Add indexes for user1_id and user2_id in matches table
  - Implement pagination for potential matches query
  - Add database constraints to prevent data corruption
  - Test query performance with sample data
  - _Requirements: 6.1, 6.4_

- [x] 11. Implement concurrency handling


  - ~~Add @Transactional annotations to prevent race conditions~~ ✓ Already implemented
  - Implement proper locking for match creation operations using @Lock annotation
  - Add retry logic for concurrent swipe scenarios
  - Write tests for concurrent user swipe scenarios
  - _Requirements: 6.3, 6.4_

- [x] 12. Add timestamp tracking to Match entity





  - Add createdAt field to Match entity with @CreationTimestamp annotation
  - Update database schema to include created_at column
  - Update MatchDTO to include matchedAt timestamp field
  - Update MatchController to return matchedAt timestamp in response
  - Update tests to verify timestamp functionality
  - _Requirements: 4.2_

- [-] 13. Create comprehensive integration tests



  - Write @SpringBootTest integration tests for complete swipe workflow
  - Test mutual matching scenarios with multiple users using real database
  - Test edge cases like no available matches with integration tests
  - Test database rollback scenarios in concurrent operations
  - Add end-to-end API tests using TestRestTemplate or WebTestClient
  - _Requirements: 1.3, 2.4, 3.1, 4.4, 5.3, 6.1_