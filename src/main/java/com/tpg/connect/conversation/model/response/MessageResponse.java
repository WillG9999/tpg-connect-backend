package com.tpg.connect.conversation.model.response;

import java.time.Instant;

public record MessageResponse(
        String messageId,
        Long senderId,
        String content,
        Instant timestamp,
        Instant readAt
) {
}
