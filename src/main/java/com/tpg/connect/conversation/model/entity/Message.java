package com.tpg.connect.conversation.model.entity;

import lombok.Builder;

import java.time.Instant;

@Builder
public record Message(
        String messageId,
        String conversationId,
        Long senderId,
        String content,
        Instant timestamp,
        Instant readAt
) {
}
