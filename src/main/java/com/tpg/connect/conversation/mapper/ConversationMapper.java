package com.tpg.connect.conversation.mapper;

import com.tpg.connect.conversation.model.entity.Conversation;
import com.tpg.connect.conversation.model.response.ConversationResponse;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ConversationMapper {

    public ConversationResponse toResponse(Conversation conversation, long currentUserId,
                                           String otherUserName, String otherUserPhotoUrl, int unreadCount) {
        Long otherUserId = conversation.participants().stream()
                .filter(id -> id != currentUserId)
                .findFirst()
                .orElse(null);

        return new ConversationResponse(
                conversation.conversationId(),
                otherUserId,
                otherUserName,
                otherUserPhotoUrl,
                conversation.lastMessageContent(),
                conversation.lastMessageSenderId(),
                conversation.lastMessageAt(),
                unreadCount
        );
    }

    @SuppressWarnings("unchecked")
    public Conversation fromDocument(Map<String, Object> data, String conversationId) {
        return Conversation.builder()
                .conversationId(conversationId)
                .participants((List<Long>) data.get("participants"))
                .createdAt(toInstant(data.get("createdAt")))
                .lastMessageAt(toInstant(data.get("lastMessageAt")))
                .lastMessageContent((String) data.get("lastMessageContent"))
                .lastMessageSenderId(toLong(data.get("lastMessageSenderId")))
                .build();
    }

    public Map<String, Object> toDocument(Conversation conversation) {
        Map<String, Object> data = new HashMap<>();
        data.put("participants", conversation.participants());
        data.put("createdAt", conversation.createdAt().toEpochMilli());
        data.put("lastMessageAt", conversation.lastMessageAt() != null ? conversation.lastMessageAt().toEpochMilli() : null);
        data.put("lastMessageContent", conversation.lastMessageContent());
        data.put("lastMessageSenderId", conversation.lastMessageSenderId());
        return data;
    }

    private Instant toInstant(Object value) {
        if (value == null) return null;
        if (value instanceof Long) return Instant.ofEpochMilli((Long) value);
        return null;
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        return null;
    }
}
