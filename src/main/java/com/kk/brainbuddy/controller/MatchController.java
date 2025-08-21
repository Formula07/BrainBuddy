package com.kk.brainbuddy.controller;

import com.kk.brainbuddy.dto.MatchDTO;
import com.kk.brainbuddy.dto.UserProfileDTO;
import com.kk.brainbuddy.entity.Match;
import com.kk.brainbuddy.entity.User;
import com.kk.brainbuddy.service.MatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for match-related operations
 * Requirements: 4.1, 4.2, 4.4, 6.2
 */
@RestController
@RequestMapping("/api/matches")
public class MatchController {
    
    private static final Logger log = LoggerFactory.getLogger(MatchController.class);
    
    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }
    
    /**
     * Get all matches for a user
     * Requirements: 4.1, 4.2, 4.4
     * 
     * @param userId the ID of the user
     * @return ResponseEntity containing list of MatchDTO objects
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<MatchDTO>> getUserMatches(@PathVariable Long userId) {
        log.debug("Getting matches for user: {}", userId);
        
        List<Match> matches = matchService.getUserMatches(userId);
        
        List<MatchDTO> matchDTOs = matches.stream()
                .map(match -> convertToMatchDTO(match, userId))
                .collect(Collectors.toList());
        
        log.debug("Found {} matches for user: {}", matchDTOs.size(), userId);
        return ResponseEntity.ok(matchDTOs);
    }
    
    /**
     * Convert Match entity to MatchDTO, determining which user is the "matched user"
     * from the perspective of the requesting user
     * 
     * @param match the Match entity
     * @param requestingUserId the ID of the user making the request
     * @return MatchDTO with the other user as the matched user
     */
    private MatchDTO convertToMatchDTO(Match match, Long requestingUserId) {
        // Determine which user is the "other" user from the perspective of the requesting user
        User matchedUser = match.getUser1().getId().equals(requestingUserId)
                ? match.getUser2() 
                : match.getUser1();
        
        UserProfileDTO matchedUserProfile = UserProfileDTO.builder()
                .id(matchedUser.getId())
                .name(matchedUser.getName())
                .bio(matchedUser.getBio())
                .build();
        
        return MatchDTO.builder()
                .matchId(match.getId())
                .matchedUser(matchedUserProfile)
                .matchedAt(match.getCreatedAt())
                .build();
    }
}