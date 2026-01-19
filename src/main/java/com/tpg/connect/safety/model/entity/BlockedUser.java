package com.tpg.connect.safety.model.entity;

import lombok.Builder;

import java.time.Instant;

@Builder
public record BlockedUser(
        String blockId,
        long blockerConnectId,
        long blockedConnectId,
        String reason,
        Instant blockedAt
) {
}

