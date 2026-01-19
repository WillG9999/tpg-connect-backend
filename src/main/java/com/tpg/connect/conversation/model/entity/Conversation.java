package com.tpg.connect.conversation.model.entity;

import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
public record Conversation(
        String conversationId,
        List<Long> participants,
        Instant createdAt,
        Instant lastMessageAt,
        String lastMessageContent,
        Long lastMessageSenderId
) {
    public static String generateConversationId(long connectId1, long connectId2) {
        long smaller = Math.min(connectId1, connectId2);
        long larger = Math.max(connectId1, connectId2);
        return smaller + "_" + larger;
    }
}
