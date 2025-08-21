package com.kk.brainbuddy.integration;

import com.kk.brainbuddy.dto.MatchDTO;
import com.kk.brainbuddy.dto.SwipeRequestDTO;
import com.kk.brainbuddy.dto.SwipeResultDTO;
import com.kk.brainbuddy.dto.UserProfileDTO;
import com.kk.brainbuddy.entity.User;
import com.kk.brainbuddy.repository.MatchRepository;
import com.kk.brainbuddy.repository.SwipeRepository;
import com.kk.brainbuddy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests using WebTestClient for reactive testing approach
 * Provides alternative testing methodology for the swipe matching system
 * Requirements: 1.3, 2.4, 3.1, 4.4, 5.3, 6.1
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
class SwipeMatchingWebClientIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SwipeRepository swipeRepository;

    @Autowired
    private MatchRepository matchRepository;

    private User user1, user2, user3;

    @BeforeEach
    void setUp() {
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
                .name("Emma Watson")
                .email("emma@example.com")
                .password("password123")
                .bio("Literature student, loves reading and writing")
                .build());

        user2 = userRepository.save(User.builder()
                .name("Ryan Gosling")
                .email("ryan@example.com")
                .password("password123")
                .bio("Film student, passionate about cinematography")
                .build());

        user3 = userRepository.save(User.builder()
                .name("Zendaya Coleman")
                .email("zendaya@example.com")
                .password("password123")
                .bio("Theater student, loves performing arts")
                .build());
    }

    /**
     * Test end-to-end API workflow using WebTestClient
     * Requirements: 1.3, 2.4, 3.1
     */
    @Test
    @WithMockUser
    void testEndToEndSwipeWorkflowWithWebClient() {
        // Step 1: Get potential match for user1
        UserProfileDTO potentialMatch = webTestClient
                .get()
                .uri("/api/swipes/potential/{userId}", user1.getId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserProfileDTO.class)
                .returnResult()
                .getResponseBody();

        assertThat(potentialMatch).isNotNull();
        assertThat(potentialMatch.getId()).isIn(user2.getId(), user3.getId());

        // Step 2: User1 swipes right on the potential match
        SwipeRequestDTO swipeRequest = SwipeRequestDTO.builder()
                .swiperId(user1.getId())
                .targetId(potentialMatch.getId())
                .liked(true)
                .build();

        SwipeResultDTO swipeResult = webTestClient
                .post()
                .uri("/api/swipes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(swipeRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(SwipeResultDTO.class)
                .returnResult()
                .getResponseBody();

        assertThat(swipeResult).isNotNull();
        assertThat(swipeResult.isSuccess()).isTrue();
        assertThat(swipeResult.isMatch()).isFalse(); // No mutual match yet

        // Step 3: Target user swipes back on user1
        SwipeRequestDTO mutualSwipeRequest = SwipeRequestDTO.builder()
                .swiperId(potentialMatch.getId())
                .targetId(user1.getId())
                .liked(true)
                .build();

        SwipeResultDTO mutualSwipeResult = webTestClient
                .post()
                .uri("/api/swipes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(mutualSwipeRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(SwipeResultDTO.class)
                .returnResult()
                .getResponseBody();

        assertThat(mutualSwipeResult).isNotNull();
        assertThat(mutualSwipeResult.isSuccess()).isTrue();
        assertThat(mutualSwipeResult.isMatch()).isTrue(); // Should be a match!

        // Step 4: Verify matches are retrievable
        List<MatchDTO> user1Matches = webTestClient
                .get()
                .uri("/api/matches/user/{userId}", user1.getId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<MatchDTO>>() {})
                .returnResult()
                .getResponseBody();

        assertThat(user1Matches).hasSize(1);
        assertThat(user1Matches.get(0).getMatchedUser().getId()).isEqualTo(potentialMatch.getId());
    }

    /**
     * Test error handling scenarios with WebTestClient
     * Requirements: 6.1
     */
    @Test
    @WithMockUser
    void testErrorHandlingScenarios() {
        // Test invalid user ID for potential matches
        webTestClient
                .get()
                .uri("/api/swipes/potential/{userId}", 999L)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.error").isEqualTo("USER_NOT_FOUND");

        // Test invalid swipe request
        SwipeRequestDTO invalidSwipeRequest = SwipeRequestDTO.builder()
                .swiperId(null) // Invalid null swiper ID
                .targetId(user2.getId())
                .liked(true)
                .build();

        webTestClient
                .post()
                .uri("/api/swipes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidSwipeRequest)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("VALIDATION_ERROR");

        // Test duplicate swipe
        SwipeRequestDTO firstSwipe = SwipeRequestDTO.builder()
                .swiperId(user1.getId())
                .targetId(user2.getId())
                .liked(true)
                .build();

        // First swipe should succeed
        webTestClient
                .post()
                .uri("/api/swipes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(firstSwipe)
                .exchange()
                .expectStatus().isOk();

        // Second swipe should fail with conflict
        webTestClient
                .post()
                .uri("/api/swipes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(firstSwipe)
                .exchange()
                .expectStatus().isEqualTo(409) // Conflict
                .expectBody()
                .jsonPath("$.error").isEqualTo("DUPLICATE_SWIPE");
    }

    /**
     * Test response time requirements
     * Requirements: 6.1
     */
    @Test
    @WithMockUser
    void testResponseTimeRequirements() {
        long startTime = System.currentTimeMillis();

        // Test potential match retrieval time
        webTestClient
                .get()
                .uri("/api/swipes/potential/{userId}", user1.getId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserProfileDTO.class);

        long potentialMatchTime = System.currentTimeMillis() - startTime;
        assertThat(potentialMatchTime).isLessThan(2000); // Should be under 2 seconds

        // Test swipe operation time
        startTime = System.currentTimeMillis();

        SwipeRequestDTO swipeRequest = SwipeRequestDTO.builder()
                .swiperId(user1.getId())
                .targetId(user2.getId())
                .liked(true)
                .build();

        webTestClient
                .post()
                .uri("/api/swipes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(swipeRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(SwipeResultDTO.class);

        long swipeTime = System.currentTimeMillis() - startTime;
        assertThat(swipeTime).isLessThan(2000); // Should be under 2 seconds

        // Test match retrieval time
        startTime = System.currentTimeMillis();

        webTestClient
                .get()
                .uri("/api/matches/user/{userId}", user1.getId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<MatchDTO>>() {});

        long matchRetrievalTime = System.currentTimeMillis() - startTime;
        assertThat(matchRetrievalTime).isLessThan(2000); // Should be under 2 seconds
    }

    /**
     * Test data consistency across operations
     * Requirements: 4.4, 5.3
     */
    @Test
    @WithMockUser
    void testDataConsistencyAcrossOperations() {
        // Create multiple swipes and verify database consistency
        SwipeRequestDTO swipe1 = SwipeRequestDTO.builder()
                .swiperId(user1.getId())
                .targetId(user2.getId())
                .liked(true)
                .build();

        SwipeRequestDTO swipe2 = SwipeRequestDTO.builder()
                .swiperId(user1.getId())
                .targetId(user3.getId())
                .liked(false)
                .build();

        SwipeRequestDTO swipe3 = SwipeRequestDTO.builder()
                .swiperId(user2.getId())
                .targetId(user1.getId())
                .liked(true)
                .build();

        // Execute swipes
        webTestClient.post().uri("/api/swipes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(swipe1)
                .exchange()
                .expectStatus().isOk();

        webTestClient.post().uri("/api/swipes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(swipe2)
                .exchange()
                .expectStatus().isOk();

        SwipeResultDTO matchResult = webTestClient.post().uri("/api/swipes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(swipe3)
                .exchange()
                .expectStatus().isOk()
                .expectBody(SwipeResultDTO.class)
                .returnResult()
                .getResponseBody();

        // Should create a match between user1 and user2
        assertThat(matchResult.isMatch()).isTrue();

        // Verify database state
        assertThat(swipeRepository.count()).isEqualTo(3);
        assertThat(matchRepository.count()).isEqualTo(1);

        // Verify user1 has exactly one match
        List<MatchDTO> user1Matches = webTestClient
                .get()
                .uri("/api/matches/user/{userId}", user1.getId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<MatchDTO>>() {})
                .returnResult()
                .getResponseBody();

        assertThat(user1Matches).hasSize(1);
        assertThat(user1Matches.get(0).getMatchedUser().getName()).isEqualTo("Ryan Gosling");

        // Verify user3 has no matches
        List<MatchDTO> user3Matches = webTestClient
                .get()
                .uri("/api/matches/user/{userId}", user3.getId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<MatchDTO>>() {})
                .returnResult()
                .getResponseBody();

        assertThat(user3Matches).isEmpty();
    }

    /**
     * Test potential match filtering logic
     * Requirements: 5.3
     */
    @Test
    @WithMockUser
    void testPotentialMatchFiltering() {
        // User1 swipes on user2 (like) and user3 (dislike)
        SwipeRequestDTO swipe1 = SwipeRequestDTO.builder()
                .swiperId(user1.getId())
                .targetId(user2.getId())
                .liked(true)
                .build();

        SwipeRequestDTO swipe2 = SwipeRequestDTO.builder()
                .swiperId(user1.getId())
                .targetId(user3.getId())
                .liked(false)
                .build();

        webTestClient.post().uri("/api/swipes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(swipe1)
                .exchange()
                .expectStatus().isOk();

        webTestClient.post().uri("/api/swipes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(swipe2)
                .exchange()
                .expectStatus().isOk();

        // Now user1 should have no more potential matches (swiped on all available users)
        webTestClient
                .get()
                .uri("/api/swipes/potential/{userId}", user1.getId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }
}