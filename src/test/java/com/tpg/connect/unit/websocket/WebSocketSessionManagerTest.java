package com.tpg.connect.unit.websocket;

import com.tpg.connect.conversation.websocket.WebSocketSessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.socket.WebSocketSession;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketSessionManagerTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private WebSocketSession webSocketSession;

    private WebSocketSessionManager sessionManager;

    @BeforeEach
    void setUp() {
        sessionManager = new WebSocketSessionManager(redisTemplate);
    }

    @Test
    void addSession_storesSessionLocally() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(webSocketSession.getId()).thenReturn("session_123");

        sessionManager.addSession(12345L, webSocketSession);

        Optional<WebSocketSession> result = sessionManager.getSession(12345L);
        assertTrue(result.isPresent());
        assertEquals(webSocketSession, result.get());
    }

    @Test
    void addSession_storesInRedis() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(webSocketSession.getId()).thenReturn("session_123");

        sessionManager.addSession(12345L, webSocketSession);

        verify(valueOperations).set(eq("ws:online:12345"), eq("session_123"), anyLong(), any());
    }

    @Test
    void removeSession_removesFromLocalAndRedis() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(webSocketSession.getId()).thenReturn("session_123");

        sessionManager.addSession(12345L, webSocketSession);
        sessionManager.removeSession(12345L);

        Optional<WebSocketSession> result = sessionManager.getSession(12345L);
        assertFalse(result.isPresent());
        verify(redisTemplate).delete("ws:online:12345");
    }

    @Test
    void getSession_returnsEmptyWhenNotFound() {
        Optional<WebSocketSession> result = sessionManager.getSession(99999L);

        assertFalse(result.isPresent());
    }

    @Test
    void isUserOnline_returnsTrueWhenLocalSessionOpen() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(webSocketSession.getId()).thenReturn("session_123");
        when(webSocketSession.isOpen()).thenReturn(true);

        sessionManager.addSession(12345L, webSocketSession);

        assertTrue(sessionManager.isUserOnline(12345L));
    }

    @Test
    void isUserOnline_returnsFalseWhenLocalSessionClosed() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(webSocketSession.getId()).thenReturn("session_123");
        when(webSocketSession.isOpen()).thenReturn(false);
        when(redisTemplate.hasKey("ws:online:12345")).thenReturn(false);

        sessionManager.addSession(12345L, webSocketSession);

        assertFalse(sessionManager.isUserOnline(12345L));
    }

    @Test
    void isUserOnline_checksRedisWhenNoLocalSession() {
        when(redisTemplate.hasKey("ws:online:12345")).thenReturn(true);

        assertTrue(sessionManager.isUserOnline(12345L));
        verify(redisTemplate).hasKey("ws:online:12345");
    }

    @Test
    void isUserOnline_returnsFalseWhenNotInRedis() {
        when(redisTemplate.hasKey("ws:online:12345")).thenReturn(false);

        assertFalse(sessionManager.isUserOnline(12345L));
    }

    @Test
    void refreshSession_extendsRedisTtl() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(webSocketSession.getId()).thenReturn("session_123");

        sessionManager.addSession(12345L, webSocketSession);
        sessionManager.refreshSession(12345L);

        verify(redisTemplate).expire(eq("ws:online:12345"), anyLong(), any());
    }

    @Test
    void refreshSession_doesNothingWhenNoLocalSession() {
        sessionManager.refreshSession(12345L);

        verify(redisTemplate, never()).expire(anyString(), anyLong(), any());
    }
}
