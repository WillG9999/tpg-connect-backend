package com.tpg.connect.matching.service;

import com.tpg.connect.conversation.model.entity.Conversation;
import com.tpg.connect.conversation.repository.ConversationRepository;
import com.tpg.connect.matching.model.entity.MatchAction;
import com.tpg.connect.matching.model.entity.MutualMatch;
import com.tpg.connect.matching.repository.MatchActionRepository;
import com.tpg.connect.matching.repository.MutualMatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchBatchProcessor {

    private final MatchActionRepository matchActionRepository;
    private final MutualMatchRepository mutualMatchRepository;
    private final ConversationRepository conversationRepository;

    @Scheduled(fixedRate = 900000)
    public void processPendingMatches() {
        log.info("Starting batch match processing...");

        List<MatchAction> unprocessedLikes = matchActionRepository.findUnprocessedLikes();
        log.info("Found {} unprocessed likes", unprocessedLikes.size());

        Set<String> processedPairs = new HashSet<>();
        int matchesCreated = 0;

        for (MatchAction like : unprocessedLikes) {
            String pairKey = createPairKey(like.actorConnectId(), like.targetConnectId());

            if (processedPairs.contains(pairKey)) {
                continue;
            }

            if (mutualMatchRepository.exists(like.actorConnectId(), like.targetConnectId())) {
                matchActionRepository.markAsProcessed(like.actionId());
                continue;
            }

            boolean isMutual = matchActionRepository.hasLiked(like.targetConnectId(), like.actorConnectId());

            if (isMutual) {
                MutualMatch match = createMutualMatch(like.actorConnectId(), like.targetConnectId());
                if (match != null) {
                    matchesCreated++;
                    processedPairs.add(pairKey);
                }
            }

            matchActionRepository.markAsProcessed(like.actionId());
        }

        log.info("Batch processing complete. Created {} new matches.", matchesCreated);
    }

    private MutualMatch createMutualMatch(long connectId1, long connectId2) {
        try {
            long lower = Math.min(connectId1, connectId2);
            long higher = Math.max(connectId1, connectId2);
            String matchId = lower + "_" + higher;
            String conversationId = matchId;

            Conversation conversation = Conversation.builder()
                    .conversationId(conversationId)
                    .participants(List.of(lower, higher))
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
            log.info("Created mutual match: {} <-> {}", lower, higher);
            return mutualMatch;
        } catch (Exception e) {
            log.error("Failed to create mutual match between {} and {}", connectId1, connectId2, e);
            return null;
        }
    }

    private String createPairKey(long id1, long id2) {
        long lower = Math.min(id1, id2);
        long higher = Math.max(id1, id2);
        return lower + "_" + higher;
    }
}

