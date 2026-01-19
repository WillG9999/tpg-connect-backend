package com.tpg.connect.conversation.mapper;

import com.tpg.connect.conversation.model.entity.Message;
import com.tpg.connect.conversation.model.response.MessageResponse;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
public class MessageMapper {

    public MessageResponse toResponse(Message message) {
        return new MessageResponse(
                message.messageId(),
                message.senderId(),
                message.content(),
                message.timestamp(),
                message.readAt()
        );
    }

    public Message fromDocument(Map<String, Object> data, String messageId) {
        return Message.builder()
                .messageId(messageId)
                .conversationId((String) data.get("conversationId"))
                .senderId(toLong(data.get("senderId")))
                .content((String) data.get("content"))
                .timestamp(toInstant(data.get("timestamp")))
                .readAt(toInstant(data.get("readAt")))
                .build();
    }

    public Map<String, Object> toDocument(Message message) {
        Map<String, Object> data = new HashMap<>();
        data.put("conversationId", message.conversationId());
        data.put("senderId", message.senderId());
        data.put("content", message.content());
        data.put("timestamp", message.timestamp().toEpochMilli());
        data.put("readAt", message.readAt() != null ? message.readAt().toEpochMilli() : null);
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
