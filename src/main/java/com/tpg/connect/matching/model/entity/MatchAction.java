package com.tpg.connect.matching.model.entity;

import lombok.Builder;

import java.time.Instant;

@Builder
public record MatchAction(
        String actionId,
        long actorConnectId,
        long targetConnectId,
        ActionType action,
        String date,
        Instant createdAt,
        boolean processed
) {
    public enum ActionType {
        LIKE,
        PASS
    }
}

