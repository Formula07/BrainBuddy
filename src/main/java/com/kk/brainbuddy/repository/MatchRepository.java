package com.kk.brainbuddy.repository;

import com.kk.brainbuddy.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    
    /**
     * Find all matches for a user (where user is either user1 or user2)
     * Ordered by most recent match first
     * Requirements: 4.1
     */
    @Query("SELECT m FROM Match m WHERE m.user1.id = :userId OR m.user2.id = :userId ORDER BY m.id DESC")
    List<Match> findMatchesByUserId(@Param("userId") Long userId);
    
    /**
     * Check if two users are already matched (prevents duplicate matches)
     * Checks both possible combinations since match can have users in either order
     * Requirements: 3.1
     */
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM Match m " +
           "WHERE (m.user1.id = :user1Id AND m.user2.id = :user2Id) " +
           "OR (m.user1.id = :user2Id AND m.user2.id = :user1Id)")
    boolean areUsersMatched(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);
}