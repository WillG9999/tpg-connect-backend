package com.tpg.connect.unit.service;

import com.tpg.connect.conversation.repository.ConversationRepository;
import com.tpg.connect.matching.model.entity.MatchAction;
import com.tpg.connect.matching.model.entity.MutualMatch;
import com.tpg.connect.matching.model.response.LikeActionResponse;
import com.tpg.connect.matching.model.response.ReceivedLikesResponse;
import com.tpg.connect.matching.repository.MatchActionRepository;
import com.tpg.connect.matching.repository.MutualMatchRepository;
import com.tpg.connect.matching.service.LikesService;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikesServiceTest {

    @Mock
    private MatchActionRepository matchActionRepository;
    @Mock
    private MutualMatchRepository mutualMatchRepository;
    @Mock
    private ConversationRepository conversationRepository;
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private SafetyService safetyService;

    private LikesService likesService;

    @BeforeEach
    void setUp() {
        likesService = new LikesService(
                matchActionRepository,
                mutualMatchRepository,
                conversationRepository,
                profileRepository,
                safetyService
        );
    }

    @Test
    void getReceivedLikes_returnsLikesWithProfiles() {
        MatchAction like = MatchAction.builder()
                .actionId("like1")
                .actorConnectId(67890L)
                .targetConnectId(12345L)
                .action(MatchAction.ActionType.LIKE)
                .createdAt(Instant.now())
                .build();

        UserProfile profile = UserProfile.builder()
                .connectId(67890L)
                .firstName("Jane")
                .dateOfBirth("1995-05-15")
                .location("New York")
                .photoUrls(List.of("http://photo.jpg"))
                .build();

        when(matchActionRepository.findLikesReceivedBy(12345L)).thenReturn(List.of(like));
        when(safetyService.getAllBlockRelatedUserIds(12345L)).thenReturn(Set.of());
        when(profileRepository.findByConnectId(67890L)).thenReturn(Optional.of(profile));

        ReceivedLikesResponse response = likesService.getReceivedLikes(12345L);

        assertTrue(response.success());
        assertEquals(1, response.likes().size());
        assertEquals("Jane", response.likes().get(0).firstName());
        assertEquals(67890L, response.likes().get(0).likerConnectId());
    }

    @Test
    void getReceivedLikes_filtersBlockedUsers() {
        MatchAction like = MatchAction.builder()
                .actionId("like1")
                .actorConnectId(67890L)
                .targetConnectId(12345L)
                .action(MatchAction.ActionType.LIKE)
                .createdAt(Instant.now())
                .build();

        when(matchActionRepository.findLikesReceivedBy(12345L)).thenReturn(List.of(like));
        when(safetyService.getAllBlockRelatedUserIds(12345L)).thenReturn(Set.of(67890L));

        ReceivedLikesResponse response = likesService.getReceivedLikes(12345L);

        assertTrue(response.success());
        assertTrue(response.likes().isEmpty());
    }

    @Test
    void getReceivedLikes_returnsEmptyWhenNoLikes() {
        when(matchActionRepository.findLikesReceivedBy(12345L)).thenReturn(List.of());
        when(safetyService.getAllBlockRelatedUserIds(12345L)).thenReturn(Set.of());

        ReceivedLikesResponse response = likesService.getReceivedLikes(12345L);

        assertTrue(response.success());
        assertTrue(response.likes().isEmpty());
    }

    @Test
    void likeBack_createsMatchWhenLikeExists() {
        UserProfile likerProfile = UserProfile.builder()
                .connectId(67890L)
                .firstName("Jane")
                .build();

        when(mutualMatchRepository.exists(12345L, 67890L)).thenReturn(false);
        when(matchActionRepository.hasLiked(67890L, 12345L)).thenReturn(true);
        when(matchActionRepository.markLikeAsActioned(67890L, 12345L, "LIKED_BACK")).thenReturn(true);
        when(matchActionRepository.save(any())).thenReturn(MatchAction.builder().build());
        when(conversationRepository.save(any())).thenReturn(true);
        when(mutualMatchRepository.save(any())).thenReturn(MutualMatch.builder()
                .matchId("12345_67890")
                .conversationId("12345_67890")
                .build());
        when(profileRepository.findByConnectId(67890L)).thenReturn(Optional.of(likerProfile));

        LikeActionResponse response = likesService.likeBack(12345L, 67890L);

        assertTrue(response.success());
        assertEquals("It's a match!", response.message());
        assertNotNull(response.newMatch());
        assertEquals("Jane", response.newMatch().matchedName());
        verify(mutualMatchRepository).save(any());
        verify(conversationRepository).save(any());
    }

    @Test
    void likeBack_returnsErrorWhenNoLikeExists() {
        when(mutualMatchRepository.exists(12345L, 67890L)).thenReturn(false);
        when(matchActionRepository.hasLiked(67890L, 12345L)).thenReturn(false);

        LikeActionResponse response = likesService.likeBack(12345L, 67890L);

        assertFalse(response.success());
        assertEquals("No like found from this user", response.message());
    }

    @Test
    void likeBack_returnsSuccessWhenAlreadyMatched() {
        when(mutualMatchRepository.exists(12345L, 67890L)).thenReturn(true);

        LikeActionResponse response = likesService.likeBack(12345L, 67890L);

        assertTrue(response.success());
        assertEquals("Already matched", response.message());
    }

    @Test
    void passLike_marksLikeAsPassed() {
        when(matchActionRepository.hasLiked(67890L, 12345L)).thenReturn(true);
        when(matchActionRepository.markLikeAsActioned(67890L, 12345L, "PASSED")).thenReturn(true);

        LikeActionResponse response = likesService.passLike(12345L, 67890L);

        assertTrue(response.success());
        assertEquals("Passed", response.message());
        verify(matchActionRepository).markLikeAsActioned(67890L, 12345L, "PASSED");
    }

    @Test
    void passLike_returnsErrorWhenNoLikeExists() {
        when(matchActionRepository.hasLiked(67890L, 12345L)).thenReturn(false);

        LikeActionResponse response = likesService.passLike(12345L, 67890L);

        assertFalse(response.success());
        assertEquals("No like found from this user", response.message());
    }
}

