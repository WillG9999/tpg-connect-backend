package com.tpg.connect.safety.model.entity;

import lombok.Builder;

import java.time.Instant;

@Builder
public record Block(
        String blockId,
        long blockerConnectId,
        long blockedConnectId,
        Instant createdAt
) {
}
