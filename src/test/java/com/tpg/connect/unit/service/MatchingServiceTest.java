package com.tpg.connect.unit.service;

import com.tpg.connect.conversation.repository.ConversationRepository;
import com.tpg.connect.matching.model.entity.DailySuggestion;
import com.tpg.connect.matching.model.entity.MatchAction;
import com.tpg.connect.matching.model.entity.MutualMatch;
import com.tpg.connect.matching.model.request.SubmitActionsRequest;
import com.tpg.connect.matching.model.response.DailySuggestionsResponse;
import com.tpg.connect.matching.model.response.MatchesListResponse;
import com.tpg.connect.matching.model.response.SubmitActionsResponse;
import com.tpg.connect.matching.repository.MatchActionRepository;
import com.tpg.connect.matching.repository.MutualMatchRepository;
import com.tpg.connect.matching.repository.UserMatchPoolRepository;
import com.tpg.connect.matching.service.MatchingService;
import com.tpg.connect.profile.model.entity.UserProfile;
import com.tpg.connect.profile.repository.ProfileRepository;
import com.tpg.connect.safety.service.SafetyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchingServiceTest {

    @Mock
    private UserMatchPoolRepository userMatchPoolRepository;
    @Mock
    private MatchActionRepository matchActionRepository;
    @Mock
    private MutualMatchRepository mutualMatchRepository;
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private ConversationRepository conversationRepository;
    @Mock
    private SafetyService safetyService;

    private MatchingService matchingService;

    @BeforeEach
    void setUp() {
        matchingService = new MatchingService(
                userMatchPoolRepository,
                matchActionRepository,
                mutualMatchRepository,
                profileRepository,
                conversationRepository,
                safetyService
        );
    }

    @Test
    void getDailySuggestions_returnsEmptyWhenNoSuggestions() {
        when(userMatchPoolRepository.getTodaysSuggestions(12345L)).thenReturn(Optional.empty());

        DailySuggestionsResponse response = matchingService.getDailySuggestions(12345L);

        assertTrue(response.success());
        assertTrue(response.suggestions().isEmpty());
    }

    @Test
    void getDailySuggestions_returnsSuggestionsWithProfiles() {
        DailySuggestion suggestion = DailySuggestion.builder()
                .suggestedConnectId(67890L)
                .compatibilityScore(0.85)
                .stabilityRank(1)
                .build();

        UserProfile profile = UserProfile.builder()
                .connectId(67890L)
                .firstName("Jane")
                .dateOfBirth("1995-05-15")
                .location("New York")
                .build();

        when(userMatchPoolRepository.getTodaysSuggestions(12345L)).thenReturn(Optional.of(List.of(suggestion)));
        when(safetyService.getAllBlockRelatedUserIds(12345L)).thenReturn(Set.of());
        when(matchActionRepository.findActedOnToday(12345L)).thenReturn(List.of());
        when(profileRepository.findByConnectId(67890L)).thenReturn(Optional.of(profile));

        DailySuggestionsResponse response = matchingService.getDailySuggestions(12345L);

        assertTrue(response.success());
        assertEquals(1, response.suggestions().size());
        assertEquals(67890L, response.suggestions().get(0).getSuggestedConnectId());
    }

    @Test
    void getDailySuggestions_filtersBlockedUsers() {
        DailySuggestion suggestion = DailySuggestion.builder()
                .suggestedConnectId(67890L)
                .compatibilityScore(0.85)
                .build();

        when(userMatchPoolRepository.getTodaysSuggestions(12345L)).thenReturn(Optional.of(List.of(suggestion)));
        when(safetyService.getAllBlockRelatedUserIds(12345L)).thenReturn(Set.of(67890L));
        when(matchActionRepository.findActedOnToday(12345L)).thenReturn(List.of());

        DailySuggestionsResponse response = matchingService.getDailySuggestions(12345L);

        assertTrue(response.success());
        assertTrue(response.suggestions().isEmpty());
    }

    @Test
    void submitActions_savesActionsAndChecksForMutualMatch() {
        SubmitActionsRequest request = new SubmitActionsRequest(
                List.of(new SubmitActionsRequest.ActionItem(67890L, MatchAction.ActionType.LIKE))
        );

        when(matchActionRepository.save(any())).thenReturn(MatchAction.builder().build());
        when(mutualMatchRepository.exists(12345L, 67890L)).thenReturn(false);
        when(matchActionRepository.hasLiked(67890L, 12345L)).thenReturn(false);

        SubmitActionsResponse response = matchingService.submitActions(12345L, request);

        assertTrue(response.success());
        assertEquals(1, response.processedCount());
        assertTrue(response.newMatches().isEmpty());
        verify(matchActionRepository).save(any());
    }

    @Test
    void submitActions_createsMutualMatchWhenBothLiked() {
        SubmitActionsRequest request = new SubmitActionsRequest(
                List.of(new SubmitActionsRequest.ActionItem(67890L, MatchAction.ActionType.LIKE))
        );

        UserProfile targetProfile = UserProfile.builder()
                .connectId(67890L)
                .firstName("Jane")
                .build();

        when(matchActionRepository.save(any())).thenReturn(MatchAction.builder().build());
        when(mutualMatchRepository.exists(12345L, 67890L)).thenReturn(false);
        when(matchActionRepository.hasLiked(67890L, 12345L)).thenReturn(true);
        when(conversationRepository.save(any())).thenReturn(true);
        when(mutualMatchRepository.save(any())).thenReturn(MutualMatch.builder()
                .matchId("12345_67890")
                .conversationId("12345_67890")
                .build());
        when(matchActionRepository.findLike(67890L, 12345L)).thenReturn(
                Optional.of(MatchAction.builder().actionId("action1").build())
        );
        when(matchActionRepository.markAsProcessed("action1")).thenReturn(true);
        when(profileRepository.findByConnectId(67890L)).thenReturn(Optional.of(targetProfile));

        SubmitActionsResponse response = matchingService.submitActions(12345L, request);

        assertTrue(response.success());
        assertEquals(1, response.newMatches().size());
        assertEquals("Jane", response.newMatches().get(0).matchedName());
        verify(mutualMatchRepository).save(any());
        verify(conversationRepository).save(any());
    }

    @Test
    void getMatches_returnsMatchesWithProfiles() {
        MutualMatch match = MutualMatch.builder()
                .matchId("12345_67890")
                .connectId1(12345L)
                .connectId2(67890L)
                .compatibilityScore(0.9)
                .conversationId("12345_67890")
                .matchedAt(Instant.now())
                .build();

        UserProfile profile = UserProfile.builder()
                .connectId(67890L)
                .firstName("Jane")
                .dateOfBirth("1995-05-15")
                .location("New York")
                .photoUrls(List.of("http://photo.jpg"))
                .build();

        when(mutualMatchRepository.findByConnectId(12345L)).thenReturn(List.of(match));
        when(profileRepository.findByConnectId(67890L)).thenReturn(Optional.of(profile));

        MatchesListResponse response = matchingService.getMatches(12345L);

        assertTrue(response.success());
        assertEquals(1, response.matches().size());
        assertEquals("Jane", response.matches().get(0).firstName());
        assertEquals(67890L, response.matches().get(0).matchedConnectId());
    }

    @Test
    void getMatches_returnsEmptyWhenNoMatches() {
        when(mutualMatchRepository.findByConnectId(12345L)).thenReturn(List.of());

        MatchesListResponse response = matchingService.getMatches(12345L);

        assertTrue(response.success());
        assertTrue(response.matches().isEmpty());
    }
}

