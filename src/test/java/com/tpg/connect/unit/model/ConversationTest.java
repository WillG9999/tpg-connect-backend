package com.tpg.connect.unit.model;

import com.tpg.connect.conversation.model.entity.Conversation;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConversationTest {

    @Test
    void generateConversationId_ordersIdsCorrectly() {
        String id1 = Conversation.generateConversationId(12345L, 67890L);
        String id2 = Conversation.generateConversationId(67890L, 12345L);

        assertEquals(id1, id2);
        assertEquals("12345_67890", id1);
    }

    @Test
    void generateConversationId_smallerIdFirst() {
        String id = Conversation.generateConversationId(99999L, 11111L);

        assertEquals("11111_99999", id);
    }

    @Test
    void generateConversationId_sameIds() {
        String id = Conversation.generateConversationId(12345L, 12345L);

        assertEquals("12345_12345", id);
    }

    @Test
    void builder_createsConversation() {
        Instant now = Instant.now();
        Conversation conversation = Conversation.builder()
                .conversationId("12345_67890")
                .participants(List.of(12345L, 67890L))
                .createdAt(now)
                .lastMessageAt(now)
                .lastMessageContent("Hello")
                .lastMessageSenderId(12345L)
                .build();

        assertEquals("12345_67890", conversation.conversationId());
        assertEquals(List.of(12345L, 67890L), conversation.participants());
        assertEquals(now, conversation.createdAt());
        assertEquals(now, conversation.lastMessageAt());
        assertEquals("Hello", conversation.lastMessageContent());
        assertEquals(12345L, conversation.lastMessageSenderId());
    }

    @Test
    void builder_handlesNullOptionalFields() {
        Conversation conversation = Conversation.builder()
                .conversationId("12345_67890")
                .participants(List.of(12345L, 67890L))
                .createdAt(Instant.now())
                .build();

        assertNull(conversation.lastMessageAt());
        assertNull(conversation.lastMessageContent());
        assertNull(conversation.lastMessageSenderId());
    }
}
