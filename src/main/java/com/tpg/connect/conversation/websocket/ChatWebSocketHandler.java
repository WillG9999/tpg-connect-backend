package com.tpg.connect.conversation.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tpg.connect.common.services.authentication.JsonWebTokenValidatorService;
import com.tpg.connect.conversation.model.entity.Conversation;
import com.tpg.connect.conversation.model.response.MessageResponse;
import com.tpg.connect.conversation.service.ConversationService;
import com.tpg.connect.conversation.service.MessageBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketSessionManager sessionManager;
    private final ConversationService conversationService;
    private final MessageBatchService messageBatchService;
    private final JsonWebTokenValidatorService jwtValidatorService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long connectId = extractConnectIdFromSession(session);
        if (connectId == null) {
            log.warn("WebSocket connection rejected - no valid token");
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        sessionManager.addSession(connectId, session);
        session.getAttributes().put("connectId", connectId);

        sendMessage(session, Map.of("type", "connected", "connectId", connectId));
        log.info("WebSocket connected for user: {}", connectId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long connectId = (Long) session.getAttributes().get("connectId");
        if (connectId != null) {
            sessionManager.removeSession(connectId);
            log.info("WebSocket disconnected for user: {}", connectId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long senderId = (Long) session.getAttributes().get("connectId");
        if (senderId == null) {
            return;
        }

        JsonNode payload = objectMapper.readTree(message.getPayload());
        String type = payload.has("type") ? payload.get("type").asText() : "";

        switch (type) {
            case "send_message" -> handleSendMessage(senderId, payload);
            case "typing" -> handleTyping(senderId, payload);
            case "read" -> handleRead(senderId, payload);
            default -> log.warn("Unknown message type: {}", type);
        }
    }

    private void handleSendMessage(Long senderId, JsonNode payload) {
        String conversationId = payload.get("conversationId").asText();
        String content = payload.get("content").asText();

        try {
            MessageResponse messageResponse = conversationService.sendMessage(conversationId, senderId, content);

            Optional<Conversation> conversation = conversationService.getConversation(conversationId);
            if (conversation.isPresent()) {
                Long recipientId = conversation.get().participants().stream()
                        .filter(id -> !id.equals(senderId))
                        .findFirst()
                        .orElse(null);

                if (recipientId != null) {
                    messageBatchService.queueMessage(recipientId, messageResponse);
                }

                sendToUser(senderId, Map.of(
                        "type", "message_sent",
                        "conversationId", conversationId,
                        "message", messageResponse
                ));
            }
        } catch (Exception e) {
            log.error("Failed to send message", e);
            sendToUser(senderId, Map.of("type", "error", "message", "Failed to send message"));
        }
    }

    private void handleTyping(Long senderId, JsonNode payload) {
        String conversationId = payload.get("conversationId").asText();
        boolean isTyping = payload.get("isTyping").asBoolean();

        Optional<Conversation> conversation = conversationService.getConversation(conversationId);
        if (conversation.isPresent()) {
            Long recipientId = conversation.get().participants().stream()
                    .filter(id -> !id.equals(senderId))
                    .findFirst()
                    .orElse(null);

            if (recipientId != null) {
                sendToUser(recipientId, Map.of(
                        "type", "typing",
                        "conversationId", conversationId,
                        "userId", senderId,
                        "isTyping", isTyping
                ));
            }
        }
    }

    private void handleRead(Long senderId, JsonNode payload) {
        String conversationId = payload.get("conversationId").asText();
        conversationService.markAsRead(conversationId, senderId);

        Optional<Conversation> conversation = conversationService.getConversation(conversationId);
        if (conversation.isPresent()) {
            Long recipientId = conversation.get().participants().stream()
                    .filter(id -> !id.equals(senderId))
                    .findFirst()
                    .orElse(null);

            if (recipientId != null) {
                sendToUser(recipientId, Map.of(
                        "type", "messages_read",
                        "conversationId", conversationId,
                        "readBy", senderId
                ));
            }
        }
    }

    private void sendToUser(long connectId, Object message) {
        sessionManager.getSession(connectId).ifPresent(session -> sendMessage(session, message));
    }

    private void sendMessage(WebSocketSession session, Object message) {
        try {
            if (session.isOpen()) {
                String json = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(json));
            }
        } catch (Exception e) {
            log.error("Failed to send WebSocket message", e);
        }
    }

    private Long extractConnectIdFromSession(WebSocketSession session) {
        String query = session.getUri() != null ? session.getUri().getQuery() : null;
        if (query == null) {
            return null;
        }

        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            if (pair.length == 2 && "token".equals(pair[0])) {
                try {
                    return jwtValidatorService.extractConnectId(pair[1]);
                } catch (Exception e) {
                    log.warn("Invalid JWT token in WebSocket connection");
                    return null;
                }
            }
        }
        return null;
    }
}
