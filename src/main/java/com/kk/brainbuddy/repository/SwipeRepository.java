package com.kk.brainbuddy.repository;

import com.kk.brainbuddy.entity.Swipe;
import com.kk.brainbuddy.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SwipeRepository extends JpaRepository<Swipe, Long> {
    
    /**
     * Check if user has already swiped on target user
     * Requirements: 2.3, 5.1
     */
    boolean existsBySwiper_IdAndTarget_Id(Long swiperId, Long targetId);
    
    /**
     * Find users that the current user has NOT swiped on (potential matches)
     * Excludes the user themselves and anyone they've already swiped on
     * Requirements: 1.1, 1.4, 5.1, 5.2
     */
    @Query("SELECT u FROM User u WHERE u.id != :userId AND u.id NOT IN " +
           "(SELECT s.target.id FROM Swipe s WHERE s.swiper.id = :userId)")
    List<User> findPotentialMatches(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Check if target user has liked the swiper back (for mutual match detection)
     * Requirements: 3.1
     */
    Optional<Swipe> findBySwiper_IdAndTarget_IdAndLiked(Long swiperId, Long targetId, boolean liked);
    
    /**
     * Find mutual likes between two users (both users liked each other)
     * Requirements: 3.1
     */
    @Query("SELECT s1 FROM Swipe s1 WHERE s1.swiper.id = :user1Id AND s1.target.id = :user2Id AND s1.liked = :liked " +
           "AND EXISTS (SELECT s2 FROM Swipe s2 WHERE s2.swiper.id = :user2Id AND s2.target.id = :user1Id AND s2.liked = :liked)")
    Optional<Swipe> findMutualLike(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id, @Param("liked") boolean liked);
}