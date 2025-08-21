package com.kk.brainbuddy.repository;

import com.kk.brainbuddy.entity.Match;
import com.kk.brainbuddy.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class MatchRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MatchRepository matchRepository;

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
    void findMatchesByUserId_ShouldReturnMatches_WhenUserIsUser1() {
        // Given
        Match match1 = Match.builder().user1(user1).user2(user2).build();
        Match match2 = Match.builder().user1(user1).user2(user3).build();
        Match match3 = Match.builder().user1(user3).user2(user4).build(); // Should not be returned
        
        entityManager.persistAndFlush(match1);
        entityManager.persistAndFlush(match2);
        entityManager.persistAndFlush(match3);

        // When
        List<Match> matches = matchRepository.findMatchesByUserId(user1.getId());

        // Then
        assertThat(matches).hasSize(2);
        assertThat(matches).extracting(Match::getId)
                .containsExactlyInAnyOrder(match1.getId(), match2.getId());
    }

    @Test
    void findMatchesByUserId_ShouldReturnMatches_WhenUserIsUser2() {
        // Given
        Match match1 = Match.builder().user1(user2).user2(user1).build();
        Match match2 = Match.builder().user1(user3).user2(user2).build();
        Match match3 = Match.builder().user1(user3).user2(user4).build(); // Should not be returned
        
        entityManager.persistAndFlush(match1);
        entityManager.persistAndFlush(match2);
        entityManager.persistAndFlush(match3);

        // When
        List<Match> matches = matchRepository.findMatchesByUserId(user2.getId());

        // Then
        assertThat(matches).hasSize(2);
        assertThat(matches).extracting(Match::getId)
                .containsExactlyInAnyOrder(match1.getId(), match2.getId());
    }

    @Test
    void findMatchesByUserId_ShouldReturnMatchesInDescendingOrder() {
        // Given - create matches in specific order
        Match match1 = Match.builder().user1(user1).user2(user2).build();
        entityManager.persistAndFlush(match1);
        
        Match match2 = Match.builder().user1(user1).user2(user3).build();
        entityManager.persistAndFlush(match2);
        
        Match match3 = Match.builder().user1(user1).user2(user4).build();
        entityManager.persistAndFlush(match3);

        // When
        List<Match> matches = matchRepository.findMatchesByUserId(user1.getId());

        // Then - should be ordered by ID descending (most recent first)
        assertThat(matches).hasSize(3);
        assertThat(matches.get(0).getId()).isGreaterThan(matches.get(1).getId());
        assertThat(matches.get(1).getId()).isGreaterThan(matches.get(2).getId());
    }

    @Test
    void findMatchesByUserId_ShouldReturnEmptyList_WhenNoMatches() {
        // When
        List<Match> matches = matchRepository.findMatchesByUserId(user1.getId());

        // Then
        assertThat(matches).isEmpty();
    }

    @Test
    void areUsersMatched_ShouldReturnTrue_WhenMatchExists_User1AsUser1() {
        // Given
        Match match = Match.builder().user1(user1).user2(user2).build();
        entityManager.persistAndFlush(match);

        // When
        boolean matched = matchRepository.areUsersMatched(user1.getId(), user2.getId());

        // Then
        assertThat(matched).isTrue();
    }

    @Test
    void areUsersMatched_ShouldReturnTrue_WhenMatchExists_User1AsUser2() {
        // Given
        Match match = Match.builder().user1(user2).user2(user1).build();
        entityManager.persistAndFlush(match);

        // When
        boolean matched = matchRepository.areUsersMatched(user1.getId(), user2.getId());

        // Then
        assertThat(matched).isTrue();
    }

    @Test
    void areUsersMatched_ShouldReturnFalse_WhenNoMatchExists() {
        // When
        boolean matched = matchRepository.areUsersMatched(user1.getId(), user2.getId());

        // Then
        assertThat(matched).isFalse();
    }

    @Test
    void areUsersMatched_ShouldReturnFalse_WhenMatchExistsWithDifferentUsers() {
        // Given
        Match match = Match.builder().user1(user3).user2(user4).build();
        entityManager.persistAndFlush(match);

        // When
        boolean matched = matchRepository.areUsersMatched(user1.getId(), user2.getId());

        // Then
        assertThat(matched).isFalse();
    }

    @Test
    void areUsersMatched_ShouldHandleMultipleMatches() {
        // Given - multiple matches for different user pairs
        Match match1 = Match.builder().user1(user1).user2(user2).build();
        Match match2 = Match.builder().user1(user3).user2(user4).build();
        Match match3 = Match.builder().user1(user1).user2(user3).build();
        
        entityManager.persistAndFlush(match1);
        entityManager.persistAndFlush(match2);
        entityManager.persistAndFlush(match3);

        // When & Then
        assertThat(matchRepository.areUsersMatched(user1.getId(), user2.getId())).isTrue();
        assertThat(matchRepository.areUsersMatched(user3.getId(), user4.getId())).isTrue();
        assertThat(matchRepository.areUsersMatched(user1.getId(), user3.getId())).isTrue();
        assertThat(matchRepository.areUsersMatched(user2.getId(), user4.getId())).isFalse();
    }
    
    @Test
    void save_ShouldSetCreatedAtTimestamp_WhenMatchIsSaved() {
        // Given
        LocalDateTime beforeSave = LocalDateTime.now().minusSeconds(1);
        Match match = Match.builder().user1(user1).user2(user2).build();

        // When
        Match savedMatch = entityManager.persistAndFlush(match);
        LocalDateTime afterSave = LocalDateTime.now().plusSeconds(1);

        // Then
        assertThat(savedMatch.getCreatedAt()).isNotNull();
        assertThat(savedMatch.getCreatedAt()).isAfter(beforeSave);
        assertThat(savedMatch.getCreatedAt()).isBefore(afterSave);
    }
    
    @Test
    void findMatchesByUserId_ShouldReturnMatchesWithTimestamp() {
        // Given
        Match match = Match.builder().user1(user1).user2(user2).build();
        entityManager.persistAndFlush(match);

        // When
        List<Match> matches = matchRepository.findMatchesByUserId(user1.getId());

        // Then
        assertThat(matches).hasSize(1);
        assertThat(matches.get(0).getCreatedAt()).isNotNull();
    }
}