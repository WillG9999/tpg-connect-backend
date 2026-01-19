package com.tpg.connect.unit.mapper;

import com.tpg.connect.conversation.mapper.ConversationMapper;
import com.tpg.connect.conversation.model.entity.Conversation;
import com.tpg.connect.conversation.model.response.ConversationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConversationMapperTest {

    private ConversationMapper conversationMapper;

    @BeforeEach
    void setUp() {
        conversationMapper = new ConversationMapper();
    }

    @Test
    void toResponse_mapsAllFields() {
        Instant now = Instant.now();
        Conversation conversation = Conversation.builder()
                .conversationId("12345_67890")
                .participants(List.of(12345L, 67890L))
                .createdAt(now)
                .lastMessageAt(now)
                .lastMessageContent("Hello")
                .lastMessageSenderId(12345L)
                .build();

        ConversationResponse response = conversationMapper.toResponse(
                conversation, 12345L, "Jane Doe", "http://photo.url", 5);

        assertEquals("12345_67890", response.conversationId());
        assertEquals(67890L, response.otherUserId());
        assertEquals("Jane Doe", response.otherUserName());
        assertEquals("http://photo.url", response.otherUserPhotoUrl());
        assertEquals("Hello", response.lastMessageContent());
        assertEquals(12345L, response.lastMessageSenderId());
        assertEquals(now, response.lastMessageAt());
        assertEquals(5, response.unreadCount());
    }

    @Test
    void toResponse_findsCorrectOtherUser() {
        Conversation conversation = Conversation.builder()
                .conversationId("12345_67890")
                .participants(List.of(12345L, 67890L))
                .createdAt(Instant.now())
                .build();

        ConversationResponse response = conversationMapper.toResponse(
                conversation, 67890L, "John Doe", "http://photo.url", 0);

        assertEquals(12345L, response.otherUserId());
    }

    @Test
    void fromDocument_mapsAllFields() {
        long createdAt = Instant.now().toEpochMilli();
        long lastMessageAt = Instant.now().plusSeconds(60).toEpochMilli();

        Map<String, Object> data = new HashMap<>();
        data.put("participants", List.of(12345L, 67890L));
        data.put("createdAt", createdAt);
        data.put("lastMessageAt", lastMessageAt);
        data.put("lastMessageContent", "Hello");
        data.put("lastMessageSenderId", 12345L);

        Conversation conversation = conversationMapper.fromDocument(data, "12345_67890");

        assertEquals("12345_67890", conversation.conversationId());
        assertEquals(List.of(12345L, 67890L), conversation.participants());
        assertEquals(Instant.ofEpochMilli(createdAt), conversation.createdAt());
        assertEquals(Instant.ofEpochMilli(lastMessageAt), conversation.lastMessageAt());
        assertEquals("Hello", conversation.lastMessageContent());
        assertEquals(12345L, conversation.lastMessageSenderId());
    }

    @Test
    void fromDocument_handlesNullLastMessage() {
        Map<String, Object> data = new HashMap<>();
        data.put("participants", List.of(12345L, 67890L));
        data.put("createdAt", Instant.now().toEpochMilli());
        data.put("lastMessageAt", null);
        data.put("lastMessageContent", null);
        data.put("lastMessageSenderId", null);

        Conversation conversation = conversationMapper.fromDocument(data, "12345_67890");

        assertNull(conversation.lastMessageAt());
        assertNull(conversation.lastMessageContent());
        assertNull(conversation.lastMessageSenderId());
    }

    @Test
    void toDocument_mapsAllFields() {
        Instant createdAt = Instant.now();
        Instant lastMessageAt = Instant.now().plusSeconds(60);
        Conversation conversation = Conversation.builder()
                .conversationId("12345_67890")
                .participants(List.of(12345L, 67890L))
                .createdAt(createdAt)
                .lastMessageAt(lastMessageAt)
                .lastMessageContent("Hello")
                .lastMessageSenderId(12345L)
                .build();

        Map<String, Object> doc = conversationMapper.toDocument(conversation);

        assertEquals(List.of(12345L, 67890L), doc.get("participants"));
        assertEquals(createdAt.toEpochMilli(), doc.get("createdAt"));
        assertEquals(lastMessageAt.toEpochMilli(), doc.get("lastMessageAt"));
        assertEquals("Hello", doc.get("lastMessageContent"));
        assertEquals(12345L, doc.get("lastMessageSenderId"));
    }

    @Test
    void toDocument_handlesNullLastMessageAt() {
        Conversation conversation = Conversation.builder()
                .conversationId("12345_67890")
                .participants(List.of(12345L, 67890L))
                .createdAt(Instant.now())
                .lastMessageAt(null)
                .build();

        Map<String, Object> doc = conversationMapper.toDocument(conversation);

        assertNull(doc.get("lastMessageAt"));
    }
}
