package com.tpg.connect.conversation.model.response;

import java.time.Instant;

public record ConversationResponse(
        String conversationId,
        Long otherUserId,
        String otherUserName,
        String otherUserPhotoUrl,
        String lastMessageContent,
        Long lastMessageSenderId,
        Instant lastMessageAt,
        int unreadCount
) {
}
