package com.tpg.connect.unit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tpg.connect.conversation.model.response.MessageResponse;
import com.tpg.connect.conversation.service.MessageBatchService;
import com.tpg.connect.conversation.websocket.WebSocketSessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageBatchServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private WebSocketSessionManager sessionManager;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ListOperations<String, String> listOperations;

    @Mock
    private WebSocketSession webSocketSession;

    private MessageBatchService messageBatchService;

    @BeforeEach
    void setUp() {
        messageBatchService = new MessageBatchService(redisTemplate, sessionManager, objectMapper);
    }

    @Test
    void queueMessage_addsMessageToRedis() throws Exception {
        MessageResponse message = new MessageResponse("msg_1", 12345L, "Hello", Instant.now(), null);
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(objectMapper.writeValueAsString(message)).thenReturn("{\"messageId\":\"msg_1\"}");
        when(listOperations.size("msg:batch:67890")).thenReturn(1L);

        messageBatchService.queueMessage(67890L, message);

        verify(listOperations).rightPush("msg:batch:67890", "{\"messageId\":\"msg_1\"}");
        verify(redisTemplate).expire(eq("msg:batch:67890"), anyLong(), any());
    }

    @Test
    void queueMessage_flushesBatchWhenSizeReached() throws Exception {
        MessageResponse message = new MessageResponse("msg_1", 12345L, "Hello", Instant.now(), null);
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"messageId\":\"msg_1\"}");
        when(listOperations.size("msg:batch:67890")).thenReturn(10L);
        when(listOperations.range("msg:batch:67890", 0, -1)).thenReturn(List.of("{\"messageId\":\"msg_1\"}"));
        when(sessionManager.getSession(67890L)).thenReturn(Optional.of(webSocketSession));
        when(webSocketSession.isOpen()).thenReturn(true);
        when(objectMapper.readValue(anyString(), eq(MessageResponse.class))).thenReturn(message);

        messageBatchService.queueMessage(67890L, message);

        verify(redisTemplate).delete("msg:batch:67890");
    }

    @Test
    void flushBatch_sendsMessagesToSession() throws Exception {
        MessageResponse message = new MessageResponse("msg_1", 12345L, "Hello", Instant.now(), null);
        String messageJson = "{\"messageId\":\"msg_1\"}";

        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.range("msg:batch:67890", 0, -1)).thenReturn(List.of(messageJson));
        when(sessionManager.getSession(67890L)).thenReturn(Optional.of(webSocketSession));
        when(webSocketSession.isOpen()).thenReturn(true);
        when(objectMapper.readValue(messageJson, MessageResponse.class)).thenReturn(message);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"type\":\"message_batch\"}");

        messageBatchService.flushBatch(67890L);

        verify(webSocketSession).sendMessage(any(TextMessage.class));
        verify(redisTemplate).delete("msg:batch:67890");
    }

    @Test
    void flushBatch_doesNothingWhenNoMessages() {
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.range("msg:batch:67890", 0, -1)).thenReturn(List.of());

        messageBatchService.flushBatch(67890L);

        verify(sessionManager, never()).getSession(anyLong());
    }

    @Test
    void flushBatch_doesNothingWhenSessionClosed() throws Exception {
        MessageResponse message = new MessageResponse("msg_1", 12345L, "Hello", Instant.now(), null);
        String messageJson = "{\"messageId\":\"msg_1\"}";

        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.range("msg:batch:67890", 0, -1)).thenReturn(List.of(messageJson));
        when(sessionManager.getSession(67890L)).thenReturn(Optional.of(webSocketSession));
        when(webSocketSession.isOpen()).thenReturn(false);
        when(objectMapper.readValue(messageJson, MessageResponse.class)).thenReturn(message);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"type\":\"message_batch\"}");

        messageBatchService.flushBatch(67890L);

        verify(webSocketSession, never()).sendMessage(any());
    }

    @Test
    void flushAllBatches_flushesAllKeys() {
        when(redisTemplate.keys("msg:batch:*")).thenReturn(Set.of("msg:batch:12345", "msg:batch:67890"));
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.range(anyString(), eq(0L), eq(-1L))).thenReturn(List.of());

        messageBatchService.flushAllBatches();

        verify(redisTemplate).keys("msg:batch:*");
    }
}
