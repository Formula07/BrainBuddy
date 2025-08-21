package com.kk.brainbuddy.integration;

import com.kk.brainbuddy.entity.Match;
import com.kk.brainbuddy.entity.Swipe;
import com.kk.brainbuddy.entity.User;
import com.kk.brainbuddy.exception.DuplicateSwipeException;
import com.kk.brainbuddy.exception.UserNotFoundException;
import com.kk.brainbuddy.repository.MatchRepository;
import com.kk.brainbuddy.repository.SwipeRepository;
import com.kk.brainbuddy.repository.UserRepository;
import com.kk.brainbuddy.service.MatchService;
import com.kk.brainbuddy.service.SwipeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests focusing on database transactions, rollbacks, and concurrent operations
 * Tests the system's ability to maintain data integrity under various failure scenarios
 * Requirements: 6.1, 6.3, 6.4
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
class DatabaseTransactionIntegrationTest {

    @Autowired
    private SwipeService swipeService;

    @Autowired
    private MatchService matchService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SwipeRepository swipeRepository;

    @Autowired
    private MatchRepository matchRepository;

    private User user1, user2, user3, user4;

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
                .name("Test User 1")
                .email("user1@test.com")
                .password("password")
                .bio("Test bio 1")
                .build());

        user2 = userRepository.save(User.builder()
                .name("Test User 2")
                .email("user2@test.com")
                .password("password")
                .bio("Test bio 2")
                .build());

        user3 = userRepository.save(User.builder()
                .name("Test User 3")
                .email("user3@test.com")
                .password("password")
                .bio("Test bio 3")
                .build());

        user4 = userRepository.save(User.builder()
                .name("Test User 4")
                .email("user4@test.com")
                .password("password")
                .bio("Test bio 4")
                .build());
    }

    /**
     * Test concurrent swipe operations to ensure no race conditions
     * Requirements: 6.3, 6.4
     */
    @Test
    void testConcurrentSwipeOperations() throws InterruptedException {
        int numberOfThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        try {
            // Multiple threads trying to create the same swipe simultaneously
            for (int i = 0; i < numberOfThreads; i++) {
                executor.submit(() -> {
                    try {
                        swipeService.recordSwipe(user1.getId(), user2.getId(), true);
                        successCount.incrementAndGet();
                    } catch (DuplicateSwipeException e) {
                        failureCount.incrementAndGet();
                    } catch (Exception e) {
                        // Unexpected exception
                        throw new RuntimeException("Unexpected exception in concurrent test", e);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            // Wait for all threads to complete
            boolean completed = latch.await(10, TimeUnit.SECONDS);
            assertThat(completed).isTrue();

            // Exactly one thread should succeed, others should fail with DuplicateSwipeException
            assertThat(successCount.get()).isEqualTo(1);
            assertThat(failureCount.get()).isEqualTo(numberOfThreads - 1);

            // Verify only one swipe record exists in database
            List<Swipe> swipes = swipeRepository.findAll();
            assertThat(swipes).hasSize(1);
            assertThat(swipes.get(0).getSwiper().getId()).isEqualTo(user1.getId());
            assertThat(swipes.get(0).getTarget().getId()).isEqualTo(user2.getId());

        } finally {
            executor.shutdown();
        }
    }

    /**
     * Test concurrent mutual matching to ensure only one match is created
     * Requirements: 6.3, 6.4
     */
    @Test
    void testConcurrentMutualMatching() throws InterruptedException {
        // Pre-create swipes for mutual matching scenario
        swipeRepository.save(Swipe.builder()
                .swiper(user1)
                .target(user2)
                .liked(true)
                .build());

        swipeRepository.save(Swipe.builder()
                .swiper(user2)
                .target(user1)
                .liked(true)
                .build());

        int numberOfThreads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        AtomicInteger matchCreationCount = new AtomicInteger(0);

        try {
            // Multiple threads trying to create the same match simultaneously
            for (int i = 0; i < numberOfThreads; i++) {
                executor.submit(() -> {
                    try {
                        matchService.createMatchIfMutual(user1.getId(), user2.getId());
                        matchCreationCount.incrementAndGet();
                    } catch (Exception e) {
                        // Expected for concurrent operations - some may fail due to unique constraints
                    } finally {
                        latch.countDown();
                    }
                });
            }

            // Wait for all threads to complete
            boolean completed = latch.await(10, TimeUnit.SECONDS);
            assertThat(completed).isTrue();

            // Verify only one match was created despite multiple concurrent attempts
            List<Match> matches = matchRepository.findAll();
            assertThat(matches).hasSize(1);

            Match match = matches.get(0);
            assertThat(match.getUser1().getId()).isIn(user1.getId(), user2.getId());
            assertThat(match.getUser2().getId()).isIn(user1.getId(), user2.getId());
            assertThat(match.getUser1().getId()).isNotEqualTo(match.getUser2().getId());

        } finally {
            executor.shutdown();
        }
    }

    /**
     * Test transaction rollback when database constraints are violated
     * Requirements: 6.1
     */
    @Test
    void testTransactionRollbackOnConstraintViolation() {
        // Create initial swipe
        swipeService.recordSwipe(user1.getId(), user2.getId(), true);
        
        long initialSwipeCount = swipeRepository.count();
        long initialMatchCount = matchRepository.count();

        // Attempt to create duplicate swipe should fail and rollback
        assertThatThrownBy(() -> {
            swipeService.recordSwipe(user1.getId(), user2.getId(), false);
        }).isInstanceOf(DuplicateSwipeException.class);

        // Verify database state hasn't changed
        assertThat(swipeRepository.count()).isEqualTo(initialSwipeCount);
        assertThat(matchRepository.count()).isEqualTo(initialMatchCount);
    }

    /**
     * Test rollback when user doesn't exist
     * Requirements: 6.1
     */
    @Test
    void testTransactionRollbackOnUserNotFound() {
        long initialSwipeCount = swipeRepository.count();
        long initialMatchCount = matchRepository.count();

        // Attempt to swipe with non-existent user should fail and rollback
        assertThatThrownBy(() -> {
            swipeService.recordSwipe(999L, user2.getId(), true);
        }).isInstanceOf(UserNotFoundException.class);

        // Verify database state hasn't changed
        assertThat(swipeRepository.count()).isEqualTo(initialSwipeCount);
        assertThat(matchRepository.count()).isEqualTo(initialMatchCount);

        // Test with non-existent target user
        assertThatThrownBy(() -> {
            swipeService.recordSwipe(user1.getId(), 999L, true);
        }).isInstanceOf(UserNotFoundException.class);

        // Verify database state still hasn't changed
        assertThat(swipeRepository.count()).isEqualTo(initialSwipeCount);
        assertThat(matchRepository.count()).isEqualTo(initialMatchCount);
    }

    /**
     * Test complex concurrent scenario with multiple users and operations
     * Requirements: 6.3, 6.4
     */
    @Test
    void testComplexConcurrentScenario() throws InterruptedException {
        int numberOfThreads = 20;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        AtomicInteger successfulOperations = new AtomicInteger(0);

        try {
            // Create various concurrent operations:
            // - User1 swipes on User2, User3, User4
            // - User2 swipes on User1, User3
            // - User3 swipes on User1, User2
            // - User4 swipes on User1

            Runnable[] operations = {
                () -> performSwipeOperation(user1.getId(), user2.getId(), true, successfulOperations),
                () -> performSwipeOperation(user1.getId(), user3.getId(), true, successfulOperations),
                () -> performSwipeOperation(user1.getId(), user4.getId(), false, successfulOperations),
                () -> performSwipeOperation(user2.getId(), user1.getId(), true, successfulOperations),
                () -> performSwipeOperation(user2.getId(), user3.getId(), false, successfulOperations),
                () -> performSwipeOperation(user3.getId(), user1.getId(), true, successfulOperations),
                () -> performSwipeOperation(user3.getId(), user2.getId(), true, successfulOperations),
                () -> performSwipeOperation(user4.getId(), user1.getId(), true, successfulOperations),
            };

            // Submit operations multiple times to create concurrency
            for (int i = 0; i < numberOfThreads; i++) {
                final int operationIndex = i % operations.length;
                executor.submit(() -> {
                    try {
                        operations[operationIndex].run();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            // Wait for all operations to complete
            boolean completed = latch.await(15, TimeUnit.SECONDS);
            assertThat(completed).isTrue();

            // Verify database consistency
            List<Swipe> allSwipes = swipeRepository.findAll();
            List<Match> allMatches = matchRepository.findAll();

            // Should have exactly 8 unique swipes (no duplicates due to concurrent operations)
            assertThat(allSwipes).hasSize(8);

            // Verify expected matches were created:
            // user1-user2 (mutual like), user1-user3 (mutual like), user2-user3 (mutual like)
            assertThat(allMatches).hasSize(3);

            // Verify no duplicate matches exist
            long distinctMatchCount = allMatches.stream()
                    .map(match -> {
                        Long id1 = match.getUser1().getId();
                        Long id2 = match.getUser2().getId();
                        return id1 < id2 ? id1 + "-" + id2 : id2 + "-" + id1;
                    })
                    .distinct()
                    .count();
            assertThat(distinctMatchCount).isEqualTo(allMatches.size());

        } finally {
            executor.shutdown();
        }
    }

    /**
     * Test database performance under concurrent load
     * Requirements: 6.1
     */
    @Test
    void testDatabasePerformanceUnderLoad() throws InterruptedException {
        int numberOfOperations = 100;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(numberOfOperations);
        long startTime = System.currentTimeMillis();

        try {
            // Create many users for testing
            for (int i = 5; i <= 20; i++) {
                userRepository.save(User.builder()
                        .name("Load Test User " + i)
                        .email("loadtest" + i + "@test.com")
                        .password("password")
                        .bio("Load test bio " + i)
                        .build());
            }

            List<User> allUsers = userRepository.findAll();

            // Perform many concurrent operations
            for (int i = 0; i < numberOfOperations; i++) {
                final int operationIndex = i;
                executor.submit(() -> {
                    try {
                        User swiper = allUsers.get(operationIndex % allUsers.size());
                        User target = allUsers.get((operationIndex + 1) % allUsers.size());
                        
                        if (!swiper.getId().equals(target.getId())) {
                            try {
                                swipeService.recordSwipe(swiper.getId(), target.getId(), 
                                    operationIndex % 2 == 0);
                            } catch (DuplicateSwipeException e) {
                                // Expected for some operations
                            }
                        }
                    } catch (Exception e) {
                        // Log but don't fail the test for expected exceptions
                    } finally {
                        latch.countDown();
                    }
                });
            }

            // Wait for all operations to complete
            boolean completed = latch.await(30, TimeUnit.SECONDS);
            assertThat(completed).isTrue();

            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;

            // Performance requirement: operations should complete in reasonable time
            assertThat(totalTime).isLessThan(20000); // 20 seconds for 100 operations

            // Verify database integrity after load test
            List<Swipe> allSwipes = swipeRepository.findAll();
            List<Match> allMatches = matchRepository.findAll();

            // All swipes should be unique (no duplicates despite concurrent operations)
            long distinctSwipeCount = allSwipes.stream()
                    .map(swipe -> swipe.getSwiper().getId() + "-" + swipe.getTarget().getId())
                    .distinct()
                    .count();
            assertThat(distinctSwipeCount).isEqualTo(allSwipes.size());

            // All matches should be unique
            long distinctMatchCount = allMatches.stream()
                    .map(match -> {
                        Long id1 = match.getUser1().getId();
                        Long id2 = match.getUser2().getId();
                        return id1 < id2 ? id1 + "-" + id2 : id2 + "-" + id1;
                    })
                    .distinct()
                    .count();
            assertThat(distinctMatchCount).isEqualTo(allMatches.size());

        } finally {
            executor.shutdown();
        }
    }

    /**
     * Helper method to perform swipe operation and handle exceptions
     */
    private void performSwipeOperation(Long swiperId, Long targetId, boolean liked, 
                                     AtomicInteger successCounter) {
        try {
            swipeService.recordSwipe(swiperId, targetId, liked);
            successCounter.incrementAndGet();
        } catch (DuplicateSwipeException | UserNotFoundException e) {
            // Expected exceptions in concurrent scenarios
        } catch (Exception e) {
            // Unexpected exception - let it bubble up to fail the test
            throw new RuntimeException("Unexpected exception in swipe operation", e);
        }
    }
}