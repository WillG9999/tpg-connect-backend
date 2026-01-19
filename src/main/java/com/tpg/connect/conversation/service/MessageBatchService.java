package com.tpg.connect.conversation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tpg.connect.conversation.model.response.MessageResponse;
import com.tpg.connect.conversation.websocket.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageBatchService {

    private static final String REDIS_BATCH_KEY = "msg:batch:";
    private static final int BATCH_SIZE = 10;
    private static final long BATCH_FLUSH_INTERVAL_MS = 100;

    private final RedisTemplate<String, String> redisTemplate;
    private final WebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper;

    public void queueMessage(long recipientId, MessageResponse message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            redisTemplate.opsForList().rightPush(REDIS_BATCH_KEY + recipientId, json);
            redisTemplate.expire(REDIS_BATCH_KEY + recipientId, 5, TimeUnit.MINUTES);

            long queueSize = Optional.ofNullable(
                    redisTemplate.opsForList().size(REDIS_BATCH_KEY + recipientId)
            ).orElse(0L);

            if (queueSize >= BATCH_SIZE) {
                flushBatch(recipientId);
            }
        } catch (Exception e) {
            log.error("Failed to queue message for user: {}", recipientId, e);
        }
    }

    @Scheduled(fixedRate = BATCH_FLUSH_INTERVAL_MS)
    public void flushAllBatches() {
        Set<String> keys = redisTemplate.keys(REDIS_BATCH_KEY + "*");
        if (keys != null) {
            for (String key : keys) {
                long recipientId = Long.parseLong(key.replace(REDIS_BATCH_KEY, ""));
                flushBatch(recipientId);
            }
        }
    }

    public void flushBatch(long recipientId) {
        String key = REDIS_BATCH_KEY + recipientId;
        List<String> messages = redisTemplate.opsForList().range(key, 0, -1);

        if (messages == null || messages.isEmpty()) {
            return;
        }

        redisTemplate.delete(key);

        sessionManager.getSession(recipientId).ifPresent(session -> {
            try {
                List<MessageResponse> batch = new ArrayList<>();
                for (String json : messages) {
                    batch.add(objectMapper.readValue(json, MessageResponse.class));
                }

                String payload = objectMapper.writeValueAsString(Map.of(
                        "type", "message_batch",
                        "messages", batch
                ));

                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(payload));
                    log.debug("Flushed {} messages to user: {}", batch.size(), recipientId);
                }
            } catch (Exception e) {
                log.error("Failed to flush batch for user: {}", recipientId, e);
            }
        });
    }
}
