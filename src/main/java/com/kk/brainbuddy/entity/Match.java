package com.kk.brainbuddy.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "matches",
       indexes = {
           @Index(name = "idx_match_user1_id", columnList = "user1_id"),
           @Index(name = "idx_match_user2_id", columnList = "user2_id"),
           @Index(name = "idx_match_user1_user2", columnList = "user1_id, user2_id")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_match_user1_user2", columnNames = {"user1_id", "user2_id"})
       })
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user1_id", nullable = false)
    private User user1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user2_id", nullable = false)
    private User user2;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Manual getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser1() {
        return user1;
    }

    public void setUser1(User user1) {
        this.user1 = user1;
    }

    public User getUser2() {
        return user2;
    }

    public void setUser2(User user2) {
        this.user2 = user2;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Constructors
    public Match() {}

    public Match(Long id, User user1, User user2, LocalDateTime createdAt) {
        this.id = id;
        this.user1 = user1;
        this.user2 = user2;
        this.createdAt = createdAt;
    }

    // Builder pattern
    public static MatchBuilder builder() {
        return new MatchBuilder();
    }

    public static class MatchBuilder {
        private Long id;
        private User user1;
        private User user2;
        private LocalDateTime createdAt;

        public MatchBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public MatchBuilder user1(User user1) {
            this.user1 = user1;
            return this;
        }

        public MatchBuilder user2(User user2) {
            this.user2 = user2;
            return this;
        }

        public MatchBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Match build() {
            return new Match(id, user1, user2, createdAt);
        }
    }
}