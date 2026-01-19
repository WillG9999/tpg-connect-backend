package com.tpg.connect.matching.service;

import com.tpg.connect.conversation.model.entity.Conversation;
import com.tpg.connect.conversation.repository.ConversationRepository;
import com.tpg.connect.matching.model.entity.MatchAction;
import com.tpg.connect.matching.model.entity.MutualMatch;
import com.tpg.connect.matching.model.response.LikeActionResponse;
import com.tpg.connect.matching.model.response.ReceivedLikesResponse;
import com.tpg.connect.matching.repository.MatchActionRepository;
import com.tpg.connect.matching.repository.MutualMatchRepository;
import com.tpg.connect.profile.model.entity.UserProfile;
import com.tpg.connect.profile.repository.ProfileRepository;
import com.tpg.connect.safety.service.SafetyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikesService {

    private final MatchActionRepository matchActionRepository;
    private final MutualMatchRepository mutualMatchRepository;
    private final ConversationRepository conversationRepository;
    private final ProfileRepository profileRepository;
    private final SafetyService safetyService;

    public ReceivedLikesResponse getReceivedLikes(long connectId) {
        log.info("Getting received likes for connectId: {}", connectId);

        List<MatchAction> receivedLikes = matchActionRepository.findLikesReceivedBy(connectId);

        Set<Long> blockedUserIds = safetyService.getAllBlockRelatedUserIds(connectId);

        List<ReceivedLikesResponse.ReceivedLikeItem> likeItems = receivedLikes.stream()
                .filter(like -> !blockedUserIds.contains(like.actorConnectId()))
                .map(like -> enrichLikeWithProfile(like))
                .filter(item -> item != null)
                .toList();

        return new ReceivedLikesResponse(true, likeItems, likeItems.size());
    }

    public LikeActionResponse likeBack(long connectId, long likerConnectId) {
        log.info("User {} liking back user {}", connectId, likerConnectId);

        if (mutualMatchRepository.exists(connectId, likerConnectId)) {
            return LikeActionResponse.success("Already matched");
        }

        boolean likeExists = matchActionRepository.hasLiked(likerConnectId, connectId);
        if (!likeExists) {
            return new LikeActionResponse(false, "No like found from this user", null);
        }

        matchActionRepository.markLikeAsActioned(likerConnectId, connectId, "LIKED_BACK");

        MatchAction likeBackAction = MatchAction.builder()
                .actorConnectId(connectId)
                .targetConnectId(likerConnectId)
                .action(MatchAction.ActionType.LIKE)
                .date(LocalDate.now().toString())
                .createdAt(Instant.now())
                .processed(true)
                .build();
        matchActionRepository.save(likeBackAction);

        MutualMatch match = createMutualMatch(connectId, likerConnectId);

        Optional<UserProfile> likerProfile = profileRepository.findByConnectId(likerConnectId);
        String matchedName = likerProfile.map(UserProfile::firstName).orElse("Match");

        LikeActionResponse.NewMatchInfo matchInfo = new LikeActionResponse.NewMatchInfo(
                match.matchId(),
                likerConnectId,
                matchedName,
                match.conversationId()
        );

        return LikeActionResponse.successWithMatch("It's a match!", matchInfo);
    }

    public LikeActionResponse passLike(long connectId, long likerConnectId) {
        log.info("User {} passing on like from user {}", connectId, likerConnectId);

        boolean likeExists = matchActionRepository.hasLiked(likerConnectId, connectId);
        if (!likeExists) {
            return new LikeActionResponse(false, "No like found from this user", null);
        }

        matchActionRepository.markLikeAsActioned(likerConnectId, connectId, "PASSED");

        return LikeActionResponse.success("Passed");
    }

    private MutualMatch createMutualMatch(long connectId1, long connectId2) {
        long lower = Math.min(connectId1, connectId2);
        long higher = Math.max(connectId1, connectId2);
        String matchId = lower + "_" + higher;

        Conversation conversation = Conversation.builder()
                .conversationId(matchId)
                .participants(List.of(lower, higher))
                .createdAt(Instant.now())
                .build();
        conversationRepository.save(conversation);

        MutualMatch mutualMatch = MutualMatch.builder()
                .matchId(matchId)
                .connectId1(lower)
                .connectId2(higher)
                .compatibilityScore(0.0)
                .conversationId(matchId)
                .matchedAt(Instant.now())
                .build();

        mutualMatchRepository.save(mutualMatch);
        log.info("Created mutual match: {} <-> {}", lower, higher);

        return mutualMatch;
    }

    private ReceivedLikesResponse.ReceivedLikeItem enrichLikeWithProfile(MatchAction like) {
        Optional<UserProfile> profileOpt = profileRepository.findByConnectId(like.actorConnectId());

        if (profileOpt.isEmpty()) {
            return null;
        }

        UserProfile profile = profileOpt.get();
        String primaryPhoto = profile.photoUrls() != null && !profile.photoUrls().isEmpty()
                ? profile.photoUrls().get(0)
                : null;

        return new ReceivedLikesResponse.ReceivedLikeItem(
                like.actionId(),
                like.actorConnectId(),
                profile.firstName(),
                calculateAge(profile.dateOfBirth()),
                profile.location(),
                primaryPhoto,
                profile.jobTitle(),
                profile.bio(),
                0.0,
                like.createdAt()
        );
    }

    private int calculateAge(String dateOfBirth) {
        if (dateOfBirth == null) return 0;
        try {
            LocalDate dob = LocalDate.parse(dateOfBirth);
            return Period.between(dob, LocalDate.now()).getYears();
        } catch (Exception e) {
            return 0;
        }
    }
}

