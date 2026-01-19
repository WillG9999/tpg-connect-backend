package com.tpg.connect.conversation.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketSessionManager {

    private static final String REDIS_ONLINE_USERS_KEY = "ws:online:";
    private static final long SESSION_TTL_MINUTES = 30;

    private final Map<Long, WebSocketSession> localSessions = new ConcurrentHashMap<>();
    private final RedisTemplate<String, String> redisTemplate;

    public void addSession(long connectId, WebSocketSession session) {
        localSessions.put(connectId, session);
        redisTemplate.opsForValue().set(
                REDIS_ONLINE_USERS_KEY + connectId,
                session.getId(),
                SESSION_TTL_MINUTES,
                TimeUnit.MINUTES
        );
        log.info("WebSocket session added for user: {}", connectId);
    }

    public void removeSession(long connectId) {
        localSessions.remove(connectId);
        redisTemplate.delete(REDIS_ONLINE_USERS_KEY + connectId);
        log.info("WebSocket session removed for user: {}", connectId);
    }

    public Optional<WebSocketSession> getSession(long connectId) {
        return Optional.ofNullable(localSessions.get(connectId));
    }

    public boolean isUserOnline(long connectId) {
        WebSocketSession session = localSessions.get(connectId);
        if (session != null && session.isOpen()) {
            return true;
        }
        return Boolean.TRUE.equals(redisTemplate.hasKey(REDIS_ONLINE_USERS_KEY + connectId));
    }

    public void refreshSession(long connectId) {
        if (localSessions.containsKey(connectId)) {
            redisTemplate.expire(REDIS_ONLINE_USERS_KEY + connectId, SESSION_TTL_MINUTES, TimeUnit.MINUTES);
        }
    }
}
