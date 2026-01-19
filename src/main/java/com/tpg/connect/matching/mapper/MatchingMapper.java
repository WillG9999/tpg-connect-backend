package com.tpg.connect.matching.mapper;

import com.tpg.connect.matching.model.entity.DailySuggestion;
import com.tpg.connect.matching.model.entity.MatchAction;
import com.tpg.connect.matching.model.entity.MutualMatch;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MatchingMapper {

    public Map<String, Object> matchActionToDocument(MatchAction action) {
        Map<String, Object> doc = new HashMap<>();
        doc.put("actorConnectId", action.actorConnectId());
        doc.put("targetConnectId", action.targetConnectId());
        doc.put("action", action.action().name());
        doc.put("date", action.date());
        doc.put("createdAt", action.createdAt().toEpochMilli());
        doc.put("processed", action.processed());
        return doc;
    }

    public MatchAction documentToMatchAction(Map<String, Object> data, String actionId) {
        return MatchAction.builder()
                .actionId(actionId)
                .actorConnectId(((Number) data.get("actorConnectId")).longValue())
                .targetConnectId(((Number) data.get("targetConnectId")).longValue())
                .action(MatchAction.ActionType.valueOf((String) data.get("action")))
                .date((String) data.get("date"))
                .createdAt(Instant.ofEpochMilli(((Number) data.get("createdAt")).longValue()))
                .processed(data.get("processed") != null && (Boolean) data.get("processed"))
                .build();
    }

    public Map<String, Object> mutualMatchToDocument(MutualMatch match) {
        Map<String, Object> doc = new HashMap<>();
        doc.put("connectId1", match.connectId1());
        doc.put("connectId2", match.connectId2());
        doc.put("compatibilityScore", match.compatibilityScore());
        doc.put("conversationId", match.conversationId());
        doc.put("matchedAt", match.matchedAt().toEpochMilli());
        return doc;
    }

    public MutualMatch documentToMutualMatch(Map<String, Object> data, String matchId) {
        return MutualMatch.builder()
                .matchId(matchId)
                .connectId1(((Number) data.get("connectId1")).longValue())
                .connectId2(((Number) data.get("connectId2")).longValue())
                .compatibilityScore(data.get("compatibilityScore") != null ? ((Number) data.get("compatibilityScore")).doubleValue() : 0.0)
                .conversationId((String) data.get("conversationId"))
                .matchedAt(Instant.ofEpochMilli(((Number) data.get("matchedAt")).longValue()))
                .build();
    }

    @SuppressWarnings("unchecked")
    public DailySuggestion mapPoolEntryToSuggestion(Map<String, Object> matchEntry) {
        DailySuggestion.DailySuggestionBuilder builder = DailySuggestion.builder();

        Object matchConnectId = matchEntry.get("matchConnectId");
        if (matchConnectId instanceof Number) {
            builder.suggestedConnectId(((Number) matchConnectId).longValue());
        } else if (matchConnectId instanceof String) {
            builder.suggestedConnectId(Long.parseLong((String) matchConnectId));
        }

        if (matchEntry.get("compatibilityScore") != null) {
            builder.compatibilityScore(((Number) matchEntry.get("compatibilityScore")).doubleValue());
        }
        if (matchEntry.get("stabilityRank") != null) {
            builder.stabilityRank(((Number) matchEntry.get("stabilityRank")).intValue());
        }
        builder.viewed(matchEntry.get("viewed") != null && (Boolean) matchEntry.get("viewed"));

        return builder.build();
    }
}

