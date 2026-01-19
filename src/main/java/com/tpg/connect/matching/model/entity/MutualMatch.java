package com.tpg.connect.matching.model.entity;

import lombok.Builder;

import java.time.Instant;

@Builder
public record MutualMatch(
        String matchId,
        long connectId1,
        long connectId2,
        double compatibilityScore,
        String conversationId,
        Instant matchedAt
) {
}

