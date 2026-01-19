package com.tpg.connect.matching.service;

import com.tpg.connect.conversation.model.entity.Conversation;
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
import com.tpg.connect.profile.model.entity.UserProfile;
import com.tpg.connect.profile.repository.ProfileRepository;
import com.tpg.connect.safety.service.SafetyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingService {

    private final UserMatchPoolRepository userMatchPoolRepository;
    private final MatchActionRepository matchActionRepository;
    private final MutualMatchRepository mutualMatchRepository;
    private final ProfileRepository profileRepository;
    private final ConversationRepository conversationRepository;
    private final SafetyService safetyService;

    public DailySuggestionsResponse getDailySuggestions(long connectId) {
        String today = LocalDate.now().toString();
        log.info("Getting daily suggestions for connectId: {} date: {}", connectId, today);

        Optional<List<DailySuggestion>> suggestionsOpt = userMatchPoolRepository.getTodaysSuggestions(connectId);

        if (suggestionsOpt.isEmpty()) {
            log.info("No suggestions found for connectId: {}", connectId);
            return new DailySuggestionsResponse(true, today, List.of(), 0, 0);
        }

        List<DailySuggestion> suggestions = suggestionsOpt.get();

        Set<Long> blockedUserIds = safetyService.getAllBlockRelatedUserIds(connectId);
        List<Long> actedOnToday = matchActionRepository.findActedOnToday(connectId);

        List<DailySuggestion> filteredSuggestions = suggestions.stream()
                .filter(s -> !blockedUserIds.contains(s.getSuggestedConnectId()))
                .filter(s -> !actedOnToday.contains(s.getSuggestedConnectId()))
                .toList();

        List<DailySuggestion> enrichedSuggestions = filteredSuggestions.stream()
                .map(this::enrichWithProfile)
                .toList();

        return new DailySuggestionsResponse(
                true,
                today,
                enrichedSuggestions,
                suggestions.size(),
                enrichedSuggestions.size()
        );
    }

    public SubmitActionsResponse submitActions(long connectId, SubmitActionsRequest request) {
        log.info("Submitting {} actions for connectId: {}", request.actions().size(), connectId);

        String today = LocalDate.now().toString();
        List<SubmitActionsResponse.NewMatchNotification> newMatches = new ArrayList<>();

        for (SubmitActionsRequest.ActionItem actionItem : request.actions()) {
            MatchAction action = MatchAction.builder()
                    .actorConnectId(connectId)
                    .targetConnectId(actionItem.targetConnectId())
                    .action(actionItem.action())
                    .date(today)
                    .createdAt(Instant.now())
                    .processed(false)
                    .build();

            matchActionRepository.save(action);

            if (actionItem.action() == MatchAction.ActionType.LIKE) {
                Optional<MutualMatch> mutualMatch = checkAndCreateMutualMatch(connectId, actionItem.targetConnectId());
                mutualMatch.ifPresent(match -> {
                    Optional<UserProfile> targetProfile = profileRepository.findByConnectId(actionItem.targetConnectId());
                    String matchedName = targetProfile.map(UserProfile::firstName).orElse("Match");
                    newMatches.add(new SubmitActionsResponse.NewMatchNotification(
                            match.matchId(),
                            actionItem.targetConnectId(),
                            matchedName,
                            match.conversationId()
                    ));
                });
            }
        }

        return new SubmitActionsResponse(true, request.actions().size(), newMatches);
    }

    public MatchesListResponse getMatches(long connectId) {
        log.info("Getting matches for connectId: {}", connectId);

        List<MutualMatch> matches = mutualMatchRepository.findByConnectId(connectId);

        List<MatchesListResponse.MatchItem> matchItems = matches.stream()
                .map(match -> {
                    long otherConnectId = match.connectId1() == connectId ? match.connectId2() : match.connectId1();
                    Optional<UserProfile> profileOpt = profileRepository.findByConnectId(otherConnectId);

                    return new MatchesListResponse.MatchItem(
                            match.matchId(),
                            otherConnectId,
                            profileOpt.map(UserProfile::firstName).orElse("Unknown"),
                            profileOpt.map(p -> calculateAge(p.dateOfBirth())).orElse(0),
                            profileOpt.flatMap(p -> p.photoUrls() != null && !p.photoUrls().isEmpty() ? Optional.of(p.photoUrls().get(0)) : Optional.empty()).orElse(null),
                            profileOpt.map(UserProfile::location).orElse(null),
                            match.compatibilityScore(),
                            match.conversationId(),
                            match.matchedAt()
                    );
                })
                .toList();

        return new MatchesListResponse(true, matchItems);
    }

    private Optional<MutualMatch> checkAndCreateMutualMatch(long actorConnectId, long targetConnectId) {
        if (mutualMatchRepository.exists(actorConnectId, targetConnectId)) {
            log.info("Mutual match already exists between {} and {}", actorConnectId, targetConnectId);
            return Optional.empty();
        }

        boolean targetLikedActor = matchActionRepository.hasLiked(targetConnectId, actorConnectId);

        if (targetLikedActor) {
            log.info("Mutual match found! {} <-> {}", actorConnectId, targetConnectId);

            long lower = Math.min(actorConnectId, targetConnectId);
            long higher = Math.max(actorConnectId, targetConnectId);
            String matchId = lower + "_" + higher;
            String conversationId = matchId;

            Conversation conversation = Conversation.builder()
                    .conversationId(conversationId)
                    .participants(List.of(actorConnectId, targetConnectId))
                    .createdAt(Instant.now())
                    .build();
            conversationRepository.save(conversation);

            MutualMatch mutualMatch = MutualMatch.builder()
                    .matchId(matchId)
                    .connectId1(lower)
                    .connectId2(higher)
                    .compatibilityScore(0.0)
                    .conversationId(conversationId)
                    .matchedAt(Instant.now())
                    .build();

            mutualMatchRepository.save(mutualMatch);

            matchActionRepository.findLike(targetConnectId, actorConnectId)
                    .ifPresent(action -> matchActionRepository.markAsProcessed(action.actionId()));

            return Optional.of(mutualMatch);
        }

        return Optional.empty();
    }

    private DailySuggestion enrichWithProfile(DailySuggestion suggestion) {
        Optional<UserProfile> profileOpt = profileRepository.findByConnectId(suggestion.getSuggestedConnectId());

        if (profileOpt.isEmpty()) {
            return suggestion;
        }

        UserProfile profile = profileOpt.get();
        DailySuggestion.ProfileSummary summary = DailySuggestion.ProfileSummary.builder()
                .firstName(profile.firstName())
                .age(calculateAge(profile.dateOfBirth()))
                .location(profile.location())
                .primaryPhotoUrl(profile.photoUrls() != null && !profile.photoUrls().isEmpty() ? profile.photoUrls().get(0) : null)
                .jobTitle(profile.jobTitle())
                .bio(profile.bio())
                .interests(profile.interests())
                .build();

        suggestion.setProfile(summary);
        return suggestion;
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

