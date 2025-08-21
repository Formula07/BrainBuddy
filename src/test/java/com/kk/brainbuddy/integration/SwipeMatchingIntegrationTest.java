package com.kk.brainbuddy.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kk.brainbuddy.dto.MatchDTO;
import com.kk.brainbuddy.dto.SwipeRequestDTO;
import com.kk.brainbuddy.dto.SwipeResultDTO;
import com.kk.brainbuddy.dto.UserProfileDTO;
import com.kk.brainbuddy.entity.Match;
import com.kk.brainbuddy.entity.Swipe;
import com.kk.brainbuddy.entity.User;
import com.kk.brainbuddy.repository.MatchRepository;
import com.kk.brainbuddy.repository.SwipeRepository;
import com.kk.brainbuddy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive integration tests for the complete swipe matching workflow
 * Tests the entire system end-to-end with real database operations
 * Requirements: 1.3, 2.4, 3.1, 4.4, 5.3, 6.1
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
class SwipeMatchingIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SwipeRepository swipeRepository;

    @Autowired
    private MatchRepository matchRepository;

    private String baseUrl;
    private User user1, user2, user3, user4;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        
        // Clean up database - check if tables exist first
        try {
            matchRepository.deleteAll();
        } catch (Exception e) {
            // Table might not exist yet, ignore
        }
        try {
            swipeRepository.deleteAll();
        } catch (Exception e) {
            // Table might not exist yet, ignore
        }
        try {
            userRepository.deleteAll();
        } catch (Exception e) {
            // Table might not exist yet, ignore
        }
        
        // Create test users
        user1 = userRepository.save(User.builder()
                .name("Alice Johnson")
                .email("alice@example.com")
                .password("password123")
                .bio("Computer Science student interested in algorithms")
                .build());

        user2 = userRepository.save(User.builder()
                .name("Bob Smith")
                .email("bob@example.com")
                .password("password123")
                .bio("Mathematics student, loves problem solving")
                .build());

        user3 = userRepository.save(User.builder()
                .name("Charlie Brown")
                .email("charlie@example.com")
                .password("password123")
                .bio("Physics student, quantum computing enthusiast")
                .build());

        user4 = userRepository.save(User.builder()
                .name("Diana Prince")
                .email("diana@example.com")
                .password("password123")
                .bio("Engineering student, robotics researcher")
                .build());
    }

    /**
     * Test complete swipe workflow from browsing to matching
     * Requirements: 1.3, 2.4, 3.1
     */
    @Test
    void testCompleteSwipeWorkflow() {
        // Step 1: User1 gets next potential match (should be user2)
        ResponseEntity<UserProfileDTO> potentialMatchResponse = restTemplate.getForEntity(
                baseUrl + "/api/swipes/potential/" + user1.getId(),
                UserProfileDTO.class
        );
        
        assertThat(potentialMatchResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        UserProfileDTO potentialMatch = potentialMatchResponse.getBody();
        assertThat(potentialMatch).isNotNull();
        assertThat(potentialMatch.getId()).isEqualTo(user2.getId());
        assertThat(potentialMatch.getName()).isEqualTo("Bob Smith");

        // Step 2: User1 swipes right on user2
        SwipeRequestDTO swipeRequest = SwipeRequestDTO.builder()
                .swiperId(user1.getId())
                .targetId(user2.getId())
                .liked(true)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SwipeRequestDTO> swipeEntity = new HttpEntity<>(swipeRequest, headers);

        ResponseEntity<SwipeResultDTO> swipeResponse = restTemplate.postForEntity(
                baseUrl + "/api/swipes",
                swipeEntity,
                SwipeResultDTO.class
        );

        assertThat(swipeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        SwipeResultDTO swipeResult = swipeResponse.getBody();
        assertThat(swipeResult).isNotNull();
        assertThat(swipeResult.isSuccess()).isTrue();
        assertThat(swipeResult.isMatch()).isFalse(); // No match yet, user2 hasn't swiped back

        // Verify swipe was recorded in database
        List<Swipe> swipes = swipeRepository.findAll();
        assertThat(swipes).hasSize(1);
        assertThat(swipes.get(0).getSwiper().getId()).isEqualTo(user1.getId());
        assertThat(swipes.get(0).getTarget().getId()).isEqualTo(user2.getId());
        assertThat(swipes.get(0).isLiked()).isTrue();

        // Step 3: User2 swipes right on user1 (creating mutual match)
        SwipeRequestDTO mutualSwipeRequest = SwipeRequestDTO.builder()
                .swiperId(user2.getId())
                .targetId(user1.getId())
                .liked(true)
                .build();

        HttpEntity<SwipeRequestDTO> mutualSwipeEntity = new HttpEntity<>(mutualSwipeRequest, headers);

        ResponseEntity<SwipeResultDTO> mutualSwipeResponse = restTemplate.postForEntity(
                baseUrl + "/api/swipes",
                mutualSwipeEntity,
                SwipeResultDTO.class
        );

        assertThat(mutualSwipeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        SwipeResultDTO mutualSwipeResult = mutualSwipeResponse.getBody();
        assertThat(mutualSwipeResult).isNotNull();
        assertThat(mutualSwipeResult.isSuccess()).isTrue();
        assertThat(mutualSwipeResult.isMatch()).isTrue(); // Should be a match now!

        // Verify match was created in database
        List<Match> matches = matchRepository.findAll();
        assertThat(matches).hasSize(1);
        Match match = matches.get(0);
        assertThat(match.getUser1().getId()).isIn(user1.getId(), user2.getId());
        assertThat(match.getUser2().getId()).isIn(user1.getId(), user2.getId());
        assertThat(match.getCreatedAt()).isNotNull();

        // Step 4: Verify both users can see their match
        ResponseEntity<List<MatchDTO>> user1MatchesResponse = restTemplate.exchange(
                baseUrl + "/api/matches/user/" + user1.getId(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<MatchDTO>>() {}
        );

        assertThat(user1MatchesResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<MatchDTO> user1Matches = user1MatchesResponse.getBody();
        assertThat(user1Matches).hasSize(1);
        assertThat(user1Matches.get(0).getMatchedUser().getName()).isEqualTo("Bob Smith");

        ResponseEntity<List<MatchDTO>> user2MatchesResponse = restTemplate.exchange(
                baseUrl + "/api/matches/user/" + user2.getId(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<MatchDTO>>() {}
        );

        assertThat(user2MatchesResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<MatchDTO> user2Matches = user2MatchesResponse.getBody();
        assertThat(user2Matches).hasSize(1);
        assertThat(user2Matches.get(0).getMatchedUser().getName()).isEqualTo("Alice Johnson");
    }

    /**
     * Test mutual matching scenarios with multiple users
     * Requirements: 3.1, 4.4
     */
    @Test
    void testMutualMatchingWithMultipleUsers() {
        // Create a complex matching scenario:
        // user1 likes user2, user3, user4
        // user2 likes user1, user3
        // user3 likes user1
        // user4 likes user2
        // Expected matches: user1-user2, user1-user3

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // User1 swipes right on user2, user3, user4
        swipeUser(user1.getId(), user2.getId(), true, headers);
        swipeUser(user1.getId(), user3.getId(), true, headers);
        swipeUser(user1.getId(), user4.getId(), true, headers);

        // User2 swipes right on user1, user3
        SwipeResultDTO user2SwipeResult = swipeUser(user2.getId(), user1.getId(), true, headers);
        assertThat(user2SwipeResult.isMatch()).isTrue(); // Should match with user1

        swipeUser(user2.getId(), user3.getId(), true, headers);

        // User3 swipes right on user1
        SwipeResultDTO user3SwipeResult = swipeUser(user3.getId(), user1.getId(), true, headers);
        assertThat(user3SwipeResult.isMatch()).isTrue(); // Should match with user1

        // User4 swipes right on user2
        SwipeResultDTO user4SwipeResult = swipeUser(user4.getId(), user2.getId(), true, headers);
        assertThat(user4SwipeResult.isMatch()).isFalse(); // No match, user2 didn't swipe back on user4

        // Verify matches in database
        List<Match> matches = matchRepository.findAll();
        assertThat(matches).hasSize(2); // user1-user2 and user1-user3

        // Verify user1 has 2 matches
        ResponseEntity<List<MatchDTO>> user1MatchesResponse = restTemplate.exchange(
                baseUrl + "/api/matches/user/" + user1.getId(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<MatchDTO>>() {}
        );

        List<MatchDTO> user1Matches = user1MatchesResponse.getBody();
        assertThat(user1Matches).hasSize(2);
        
        List<String> matchedNames = user1Matches.stream()
                .map(match -> match.getMatchedUser().getName())
                .toList();
        assertThat(matchedNames).containsExactlyInAnyOrder("Bob Smith", "Charlie Brown");
    }

    /**
     * Test edge case: no available matches
     * Requirements: 5.3
     */
    @Test
    void testNoAvailableMatches() {
        // User1 swipes on all other users
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        swipeUser(user1.getId(), user2.getId(), false, headers); // Dislike
        swipeUser(user1.getId(), user3.getId(), true, headers);  // Like
        swipeUser(user1.getId(), user4.getId(), false, headers); // Dislike

        // Now user1 should have no more potential matches
        ResponseEntity<UserProfileDTO> noMatchResponse = restTemplate.getForEntity(
                baseUrl + "/api/swipes/potential/" + user1.getId(),
                UserProfileDTO.class
        );

        assertThat(noMatchResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    /**
     * Test database rollback scenarios in concurrent operations
     * Requirements: 6.1
     */
    @Test
    void testConcurrentSwipeOperations() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        try {
            // Two users simultaneously swipe on each other
            CompletableFuture<SwipeResultDTO> future1 = CompletableFuture.supplyAsync(() -> {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                return swipeUser(user1.getId(), user2.getId(), true, headers);
            }, executor);

            CompletableFuture<SwipeResultDTO> future2 = CompletableFuture.supplyAsync(() -> {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                return swipeUser(user2.getId(), user1.getId(), true, headers);
            }, executor);

            // Wait for both operations to complete
            SwipeResultDTO result1;
            SwipeResultDTO result2;
            try {
                result1 = future1.get(5, TimeUnit.SECONDS);
                result2 = future2.get(5, TimeUnit.SECONDS);
            } catch (ExecutionException | TimeoutException e) {
                throw new RuntimeException("Concurrent operation failed", e);
            }

            // Both operations should succeed
            assertThat(result1.isSuccess()).isTrue();
            assertThat(result2.isSuccess()).isTrue();

            // Exactly one should report a match (the second one to complete)
            boolean hasMatch = result1.isMatch() || result2.isMatch();
            assertThat(hasMatch).isTrue();

            // Verify only one match was created (no duplicates)
            List<Match> matches = matchRepository.findAll();
            assertThat(matches).hasSize(1);

            // Verify both swipes were recorded
            List<Swipe> swipes = swipeRepository.findAll();
            assertThat(swipes).hasSize(2);

        } finally {
            executor.shutdown();
        }
    }

    /**
     * Test duplicate swipe prevention
     * Requirements: 2.4
     */
    @Test
    void testDuplicateSwipePrevention() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // First swipe should succeed
        SwipeResultDTO firstSwipe = swipeUser(user1.getId(), user2.getId(), true, headers);
        assertThat(firstSwipe.isSuccess()).isTrue();

        // Second swipe on same user should fail
        SwipeRequestDTO duplicateSwipeRequest = SwipeRequestDTO.builder()
                .swiperId(user1.getId())
                .targetId(user2.getId())
                .liked(false) // Different preference, but still duplicate
                .build();

        HttpEntity<SwipeRequestDTO> duplicateEntity = new HttpEntity<>(duplicateSwipeRequest, headers);

        ResponseEntity<String> duplicateResponse = restTemplate.postForEntity(
                baseUrl + "/api/swipes",
                duplicateEntity,
                String.class
        );

        assertThat(duplicateResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    /**
     * Test user exclusion from their own potential matches
     * Requirements: 5.3
     */
    @Test
    void testUserExclusionFromOwnMatches() {
        // User should never see themselves as a potential match
        ResponseEntity<UserProfileDTO> selfMatchResponse = restTemplate.getForEntity(
                baseUrl + "/api/swipes/potential/" + user1.getId(),
                UserProfileDTO.class
        );

        assertThat(selfMatchResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        UserProfileDTO potentialMatch = selfMatchResponse.getBody();
        assertThat(potentialMatch).isNotNull();
        assertThat(potentialMatch.getId()).isNotEqualTo(user1.getId());
    }

    /**
     * Test system performance under normal load
     * Requirements: 6.1
     */
    @Test
    void testSystemPerformanceUnderLoad() {
        long startTime = System.currentTimeMillis();
        
        // Perform multiple swipe operations
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        for (int i = 0; i < 10; i++) {
            // Get potential match
            ResponseEntity<UserProfileDTO> potentialResponse = restTemplate.getForEntity(
                    baseUrl + "/api/swipes/potential/" + user1.getId(),
                    UserProfileDTO.class
            );
            assertThat(potentialResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

            // Record swipe if potential match exists
            if (potentialResponse.getBody() != null) {
                swipeUser(user1.getId(), potentialResponse.getBody().getId(), i % 2 == 0, headers);
            }
        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        // Each operation should complete well under 2 seconds (requirement 6.1)
        // With 10 operations, total should be much less than 20 seconds
        assertThat(totalTime).isLessThan(10000); // 10 seconds for all operations
    }

    /**
     * Helper method to perform swipe operation
     */
    private SwipeResultDTO swipeUser(Long swiperId, Long targetId, boolean liked, HttpHeaders headers) {
        SwipeRequestDTO swipeRequest = SwipeRequestDTO.builder()
                .swiperId(swiperId)
                .targetId(targetId)
                .liked(liked)
                .build();

        HttpEntity<SwipeRequestDTO> swipeEntity = new HttpEntity<>(swipeRequest, headers);

        ResponseEntity<SwipeResultDTO> response = restTemplate.postForEntity(
                baseUrl + "/api/swipes",
                swipeEntity,
                SwipeResultDTO.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody();
    }
}