package com.tpg.connect.matching.model.response;

import java.time.Instant;
import java.util.List;

public record MatchesListResponse(
        boolean success,
        List<MatchItem> matches
) {
    public record MatchItem(
            String matchId,
            long matchedConnectId,
            String firstName,
            int age,
            String primaryPhotoUrl,
            String location,
            double compatibilityScore,
            String conversationId,
            Instant matchedAt
    ) {
    }
}

