package com.kk.brainbuddy.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "swipes", 
       indexes = {
           @Index(name = "idx_swipe_swiper_id", columnList = "swiper_id"),
           @Index(name = "idx_swipe_target_id", columnList = "target_id"),
           @Index(name = "idx_swipe_swiper_target", columnList = "swiper_id, target_id")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_swipe_swiper_target", columnNames = {"swiper_id", "target_id"})
       })
public class Swipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "swiper_id", nullable = false)
    private User swiper; // person who swiped

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_id", nullable = false)
    private User target; // person being swiped on

    @Column(nullable = false)
    private boolean liked; // true=like, false=dislike

    // Constructors
    public Swipe() {}

    public Swipe(Long id, User swiper, User target, boolean liked) {
        this.id = id;
        this.swiper = swiper;
        this.target = target;
        this.liked = liked;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getSwiper() {
        return swiper;
    }

    public void setSwiper(User swiper) {
        this.swiper = swiper;
    }

    public User getTarget() {
        return target;
    }

    public void setTarget(User target) {
        this.target = target;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    // Builder pattern
    public static SwipeBuilder builder() {
        return new SwipeBuilder();
    }

    public static class SwipeBuilder {
        private Long id;
        private User swiper;
        private User target;
        private boolean liked;

        public SwipeBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public SwipeBuilder swiper(User swiper) {
            this.swiper = swiper;
            return this;
        }

        public SwipeBuilder target(User target) {
            this.target = target;
            return this;
        }

        public SwipeBuilder liked(boolean liked) {
            this.liked = liked;
            return this;
        }

        public Swipe build() {
            return new Swipe(id, swiper, target, liked);
        }
    }
}
