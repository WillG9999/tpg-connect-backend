package com.tpg.connect.unit.model;

import com.tpg.connect.conversation.model.entity.Message;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class MessageTest {

    @Test
    void builder_createsMessage() {
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

        assertEquals("msg_123", message.messageId());
        assertEquals("12345_67890", message.conversationId());
        assertEquals(12345L, message.senderId());
        assertEquals("Hello World", message.content());
        assertEquals(now, message.timestamp());
        assertEquals(readAt, message.readAt());
    }

    @Test
    void builder_handlesNullReadAt() {
        Message message = Message.builder()
                .messageId("msg_123")
                .conversationId("12345_67890")
                .senderId(12345L)
                .content("Hello")
                .timestamp(Instant.now())
                .readAt(null)
                .build();

        assertNull(message.readAt());
    }

    @Test
    void builder_handlesEmptyContent() {
        Message message = Message.builder()
                .messageId("msg_123")
                .conversationId("12345_67890")
                .senderId(12345L)
                .content("")
                .timestamp(Instant.now())
                .build();

        assertEquals("", message.content());
    }
}
