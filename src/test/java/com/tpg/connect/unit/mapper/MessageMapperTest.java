package com.tpg.connect.unit.mapper;

import com.tpg.connect.conversation.mapper.MessageMapper;
import com.tpg.connect.conversation.model.entity.Message;
import com.tpg.connect.conversation.model.response.MessageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MessageMapperTest {

    private MessageMapper messageMapper;

    @BeforeEach
    void setUp() {
        messageMapper = new MessageMapper();
    }

    @Test
    void toResponse_mapsAllFields() {
        Instant now = Instant.now();
        Instant readAt = Instant.now().plusSeconds(60);
        Message message = Message.builder()
                .messageId("msg_123")
                .conversationId("12345_67890")
                .senderId(12345L)
                .content("Hello World")
                .timestamp(now)
                .readAt(readAt)
                .build();

        MessageResponse response = messageMapper.toResponse(message);

        assertEquals("msg_123", response.messageId());
        assertEquals(12345L, response.senderId());
        assertEquals("Hello World", response.content());
        assertEquals(now, response.timestamp());
        assertEquals(readAt, response.readAt());
    }

    @Test
    void toResponse_handlesNullReadAt() {
        Message message = Message.builder()
                .messageId("msg_123")
                .senderId(12345L)
                .content("Hello")
                .timestamp(Instant.now())
                .readAt(null)
                .build();

        MessageResponse response = messageMapper.toResponse(message);

        assertNull(response.readAt());
    }

    @Test
    void fromDocument_mapsAllFields() {
        long timestamp = Instant.now().toEpochMilli();
        long readAt = Instant.now().plusSeconds(60).toEpochMilli();

        Map<String, Object> data = new HashMap<>();
        data.put("conversationId", "12345_67890");
        data.put("senderId", 12345L);
        data.put("content", "Hello World");
        data.put("timestamp", timestamp);
        data.put("readAt", readAt);

        Message message = messageMapper.fromDocument(data, "msg_123");

        assertEquals("msg_123", message.messageId());
        assertEquals("12345_67890", message.conversationId());
        assertEquals(12345L, message.senderId());
        assertEquals("Hello World", message.content());
        assertEquals(Instant.ofEpochMilli(timestamp), message.timestamp());
        assertEquals(Instant.ofEpochMilli(readAt), message.readAt());
    }

    @Test
    void fromDocument_handlesNullReadAt() {
        Map<String, Object> data = new HashMap<>();
        data.put("conversationId", "12345_67890");
        data.put("senderId", 12345L);
        data.put("content", "Hello");
        data.put("timestamp", Instant.now().toEpochMilli());
        data.put("readAt", null);

        Message message = messageMapper.fromDocument(data, "msg_123");

        assertNull(message.readAt());
    }

    @Test
    void fromDocument_handlesIntegerSenderId() {
        Map<String, Object> data = new HashMap<>();
        data.put("conversationId", "12345_67890");
        data.put("senderId", 12345);
        data.put("content", "Hello");
        data.put("timestamp", Instant.now().toEpochMilli());

        Message message = messageMapper.fromDocument(data, "msg_123");

        assertEquals(12345L, message.senderId());
    }

    @Test
    void toDocument_mapsAllFields() {
        Instant now = Instant.now();
        Instant readAt = Instant.now().plusSeconds(60);
        Message message = Message.builder()
                .messageId("msg_123")
                .conversationId("12345_67890")
                .senderId(12345L)
                .content("Hello World")
                .timestamp(now)
                .readAt(readAt)
                .build();

        Map<String, Object> doc = messageMapper.toDocument(message);

        assertEquals("12345_67890", doc.get("conversationId"));
        assertEquals(12345L, doc.get("senderId"));
        assertEquals("Hello World", doc.get("content"));
        assertEquals(now.toEpochMilli(), doc.get("timestamp"));
        assertEquals(readAt.toEpochMilli(), doc.get("readAt"));
    }

    @Test
    void toDocument_handlesNullReadAt() {
        Message message = Message.builder()
                .messageId("msg_123")
                .conversationId("12345_67890")
                .senderId(12345L)
                .content("Hello")
                .timestamp(Instant.now())
                .readAt(null)
                .build();

        Map<String, Object> doc = messageMapper.toDocument(message);

        assertNull(doc.get("readAt"));
    }
}
