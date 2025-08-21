CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    bio TEXT
);

CREATE TABLE IF NOT EXISTS swipes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    swiper_id BIGINT NOT NULL,
    target_id BIGINT NOT NULL,
    liked BOOLEAN NOT NULL,
    FOREIGN KEY (swiper_id) REFERENCES users(id),
    FOREIGN KEY (target_id) REFERENCES users(id),
    UNIQUE (swiper_id, target_id)
);

-- Create indexes for swipes table
CREATE INDEX IF NOT EXISTS idx_swipe_swiper_id ON swipes(swiper_id);
CREATE INDEX IF NOT EXISTS idx_swipe_target_id ON swipes(target_id);
CREATE INDEX IF NOT EXISTS idx_swipe_swiper_target ON swipes(swiper_id, target_id);

CREATE TABLE IF NOT EXISTS matches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user1_id BIGINT NOT NULL,
    user2_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user1_id) REFERENCES users(id),
    FOREIGN KEY (user2_id) REFERENCES users(id),
    UNIQUE (user1_id, user2_id)
);

-- Create indexes for matches table
CREATE INDEX IF NOT EXISTS idx_match_user1_id ON matches(user1_id);
CREATE INDEX IF NOT EXISTS idx_match_user2_id ON matches(user2_id);
CREATE INDEX IF NOT EXISTS idx_match_user1_user2 ON matches(user1_id, user2_id);