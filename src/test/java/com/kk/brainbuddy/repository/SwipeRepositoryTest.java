package com.kk.brainbuddy.repository;

import com.kk.brainbuddy.entity.Swipe;
import com.kk.brainbuddy.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Sql(scripts = "classpath:schema.sql")
class SwipeRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SwipeRepository swipeRepository;

    private User user1, user2, user3, user4;

    @BeforeEach
    void setUp() {
        // Create test users
        user1 = User.builder()
                .email("user1@test.com")
                .password("password")
                .name("User One")
                .bio("Bio 1")
                .build();
        
        user2 = User.builder()
                .email("user2@test.com")
                .password("password")
                .name("User Two")
                .bio("Bio 2")
                .build();
        
        user3 = User.builder()
                .email("user3@test.com")
                .password("password")
                .name("User Three")
                .bio("Bio 3")
                .build();
        
        user4 = User.builder()
                .email("user4@test.com")
                .password("password")
                .name("User Four")
                .bio("Bio 4")
                .build();

        // Persist users
        user1 = entityManager.persistAndFlush(user1);
        user2 = entityManager.persistAndFlush(user2);
        user3 = entityManager.persistAndFlush(user3);
        user4 = entityManager.persistAndFlush(user4);
    }

    @Test
    void existsBySwiper_IdAndTarget_Id_ShouldReturnTrue_WhenSwipeExists() {
        // Given
        Swipe swipe = Swipe.builder()
                .swiper(user1)
                .target(user2)
                .liked(true)
                .build();
        entityManager.persistAndFlush(swipe);

        // When
        boolean exists = swipeRepository.existsBySwiper_IdAndTarget_Id(user1.getId(), user2.getId());

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsBySwiper_IdAndTarget_Id_ShouldReturnFalse_WhenSwipeDoesNotExist() {
        // When
        boolean exists = swipeRepository.existsBySwiper_IdAndTarget_Id(user1.getId(), user2.getId());

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void findPotentialMatches_ShouldReturnUsersNotSwipedOn() {
        // Given - user1 has swiped on user2 but not user3 or user4
        Swipe swipe = Swipe.builder()
                .swiper(user1)
                .target(user2)
                .liked(true)
                .build();
        entityManager.persistAndFlush(swipe);

        Pageable pageable = PageRequest.of(0, 10);

        // When
        List<User> potentialMatches = swipeRepository.findPotentialMatches(user1.getId(), pageable);

        // Then
        assertThat(potentialMatches).hasSize(2);
        assertThat(potentialMatches).extracting(User::getId)
                .containsExactlyInAnyOrder(user3.getId(), user4.getId());
        assertThat(potentialMatches).extracting(User::getId)
                .doesNotContain(user1.getId(), user2.getId()); // Should not contain self or swiped users
    }

    @Test
    void findPotentialMatches_ShouldReturnEmptyList_WhenAllUsersSwipedOn() {
        // Given - user1 has swiped on all other users
        Swipe swipe1 = Swipe.builder().swiper(user1).target(user2).liked(true).build();
        Swipe swipe2 = Swipe.builder().swiper(user1).target(user3).liked(false).build();
        Swipe swipe3 = Swipe.builder().swiper(user1).target(user4).liked(true).build();
        
        entityManager.persistAndFlush(swipe1);
        entityManager.persistAndFlush(swipe2);
        entityManager.persistAndFlush(swipe3);

        Pageable pageable = PageRequest.of(0, 10);

        // When
        List<User> potentialMatches = swipeRepository.findPotentialMatches(user1.getId(), pageable);

        // Then
        assertThat(potentialMatches).isEmpty();
    }

    @Test
    void findBySwiper_IdAndTarget_IdAndLiked_ShouldReturnSwipe_WhenLikedSwipeExists() {
        // Given
        Swipe swipe = Swipe.builder()
                .swiper(user1)
                .target(user2)
                .liked(true)
                .build();
        entityManager.persistAndFlush(swipe);

        // When
        Optional<Swipe> result = swipeRepository.findBySwiper_IdAndTarget_IdAndLiked(
                user1.getId(), user2.getId(), true);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getSwiper().getId()).isEqualTo(user1.getId());
        assertThat(result.get().getTarget().getId()).isEqualTo(user2.getId());
        assertThat(result.get().isLiked()).isTrue();
    }

    @Test
    void findBySwiper_IdAndTarget_IdAndLiked_ShouldReturnEmpty_WhenDislikedSwipeExists() {
        // Given
        Swipe swipe = Swipe.builder()
                .swiper(user1)
                .target(user2)
                .liked(false)
                .build();
        entityManager.persistAndFlush(swipe);

        // When
        Optional<Swipe> result = swipeRepository.findBySwiper_IdAndTarget_IdAndLiked(
                user1.getId(), user2.getId(), true);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findMutualLike_ShouldReturnSwipe_WhenBothUsersLikedEachOther() {
        // Given - both users liked each other
        Swipe swipe1 = Swipe.builder()
                .swiper(user1)
                .target(user2)
                .liked(true)
                .build();
        
        Swipe swipe2 = Swipe.builder()
                .swiper(user2)
                .target(user1)
                .liked(true)
                .build();
        
        entityManager.persistAndFlush(swipe1);
        entityManager.persistAndFlush(swipe2);

        // When
        Optional<Swipe> result = swipeRepository.findMutualLike(user1.getId(), user2.getId(), true);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getSwiper().getId()).isEqualTo(user1.getId());
        assertThat(result.get().getTarget().getId()).isEqualTo(user2.getId());
        assertThat(result.get().isLiked()).isTrue();
    }

    @Test
    void findMutualLike_ShouldReturnEmpty_WhenOnlyOneUserLiked() {
        // Given - only user1 liked user2
        Swipe swipe1 = Swipe.builder()
                .swiper(user1)
                .target(user2)
                .liked(true)
                .build();
        
        Swipe swipe2 = Swipe.builder()
                .swiper(user2)
                .target(user1)
                .liked(false)
                .build();
        
        entityManager.persistAndFlush(swipe1);
        entityManager.persistAndFlush(swipe2);

        // When
        Optional<Swipe> result = swipeRepository.findMutualLike(user1.getId(), user2.getId(), true);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findMutualLike_ShouldReturnEmpty_WhenNoSwipesExist() {
        // When
        Optional<Swipe> result = swipeRepository.findMutualLike(user1.getId(), user2.getId(), true);

        // Then
        assertThat(result).isEmpty();
    }
}