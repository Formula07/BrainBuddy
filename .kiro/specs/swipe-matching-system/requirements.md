# Requirements Document

## Introduction

The Swipe and Matching System is the core feature of BrainBuddy that enables users to discover and connect with potential study partners. This system allows users to browse through other users' profiles, express interest through swiping gestures (like/dislike), and automatically creates matches when mutual interest is detected. The feature builds upon the existing User, Swipe, and Match entities to provide a seamless study partner discovery experience.

## Requirements

### Requirement 1

**User Story:** As a student user, I want to browse through potential study partners one at a time, so that I can discover people who might be good study matches for me.

#### Acceptance Criteria

1. WHEN a user requests to browse potential matches THEN the system SHALL return one user profile at a time that the current user has not previously swiped on
2. WHEN displaying a potential match THEN the system SHALL show the user's name, bio, and any study-related information
3. WHEN no more potential matches are available THEN the system SHALL inform the user that they have seen all available profiles
4. WHEN a user has been swiped on by the current user THEN that user SHALL NOT appear again in the browsing queue

### Requirement 2

**User Story:** As a student user, I want to swipe right (like) or left (dislike) on potential study partners, so that I can express my interest or lack thereof in studying with them.

#### Acceptance Criteria

1. WHEN a user swipes right on a profile THEN the system SHALL record a positive swipe (liked=true) in the database
2. WHEN a user swipes left on a profile THEN the system SHALL record a negative swipe (liked=false) in the database
3. WHEN a swipe is recorded THEN the system SHALL prevent the same user from swiping on the same target user again
4. WHEN a swipe is submitted THEN the system SHALL immediately check if a mutual match has been created
5. WHEN a swipe is recorded THEN the system SHALL return the next potential match profile or indicate no more profiles are available

### Requirement 3

**User Story:** As a student user, I want matches to be automatically created when someone I liked also likes me back, so that I can connect with mutually interested study partners.

#### Acceptance Criteria

1. WHEN two users have both swiped right on each other THEN the system SHALL automatically create a Match record
2. WHEN a match is created THEN both users SHALL be notified of the new match
3. WHEN checking for matches THEN the system SHALL only create one Match record per user pair regardless of swipe order
4. WHEN a match is created THEN the system SHALL store both user references in the Match entity
5. IF a user has already been matched with another user THEN no duplicate Match records SHALL be created

### Requirement 4

**User Story:** As a student user, I want to view all my current matches, so that I can see who I've been matched with and potentially start conversations.

#### Acceptance Criteria

1. WHEN a user requests their matches THEN the system SHALL return all Match records where the user is either user1 or user2
2. WHEN displaying matches THEN the system SHALL show the matched user's profile information (name, bio)
3. WHEN retrieving matches THEN the system SHALL order them by most recent match first
4. WHEN a user has no matches THEN the system SHALL return an empty list with appropriate messaging

### Requirement 5

**User Story:** As a student user, I want the system to prevent me from seeing users I've already swiped on, so that I don't waste time reviewing the same profiles repeatedly.

#### Acceptance Criteria

1. WHEN generating potential matches THEN the system SHALL exclude users the current user has already swiped on (either liked or disliked)
2. WHEN generating potential matches THEN the system SHALL exclude the current user from their own potential matches
3. WHEN a user has swiped on all available users THEN the system SHALL return an appropriate message indicating no more profiles are available
4. WHEN new users join the platform THEN they SHALL become available in existing users' potential match queues

### Requirement 6

**User Story:** As a student user, I want the swipe and match operations to be fast and reliable, so that I have a smooth experience while browsing potential study partners.

#### Acceptance Criteria

1. WHEN a user performs a swipe action THEN the system SHALL respond within 2 seconds under normal load
2. WHEN database operations fail THEN the system SHALL return appropriate error messages without exposing technical details
3. WHEN concurrent users swipe on each other simultaneously THEN the system SHALL handle race conditions and ensure only one Match is created
4. WHEN the system is under load THEN swipe operations SHALL continue to function correctly without data corruption