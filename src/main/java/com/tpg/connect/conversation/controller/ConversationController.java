package com.tpg.connect.conversation.controller;

import com.tpg.connect.common.services.authentication.JsonWebTokenValidatorService;
import com.tpg.connect.conversation.controller.api.ConversationApi;
import com.tpg.connect.conversation.mapper.ConversationMapper;
import com.tpg.connect.conversation.model.entity.Conversation;
import com.tpg.connect.conversation.model.request.SendMessageRequest;
import com.tpg.connect.conversation.model.response.ConversationResponse;
import com.tpg.connect.conversation.model.response.MessageResponse;
import com.tpg.connect.conversation.model.response.PaginatedMessagesResponse;
import com.tpg.connect.conversation.service.ConversationService;
import com.tpg.connect.profile.repository.ProfileRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.tpg.connect.common.constants.HeaderConstants.X_AUTHORISATION;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ConversationController implements ConversationApi {

    private final ConversationService conversationService;
    private final JsonWebTokenValidatorService jwtValidatorService;
    private final ProfileRepository profileRepository;
    private final ConversationMapper conversationMapper;
    private final HttpServletRequest httpServletRequest;

    @Override
    public ResponseEntity<List<ConversationResponse>> getConversations() {
        long connectId = extractConnectId();
        log.info("Getting conversations for user: {}", connectId);

        List<ConversationResponse> conversations = conversationService.getConversationsForUser(connectId);
        return ResponseEntity.ok(conversations);
    }

    @Override
    public ResponseEntity<PaginatedMessagesResponse> getMessages(String conversationId, int limit, String cursor) {
        long connectId = extractConnectId();
        log.info("Getting messages for conversation: {} user: {} cursor: {}", conversationId, connectId, cursor);

        PaginatedMessagesResponse response = conversationService.getMessagesWithCursor(conversationId, connectId, limit, cursor);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<MessageResponse> sendMessage(String conversationId, SendMessageRequest request) {
        long connectId = extractConnectId();
        log.info("Sending message in conversation: {} from user: {}", conversationId, connectId);

        try {
            MessageResponse message = conversationService.sendMessage(conversationId, connectId, request.content());
            return ResponseEntity.ok(message);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public ResponseEntity<Void> markAsRead(String conversationId) {
        long connectId = extractConnectId();
        log.info("Marking messages as read in conversation: {} for user: {}", conversationId, connectId);

        boolean success = conversationService.markAsRead(conversationId, connectId);
        return success ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @Override
    public ResponseEntity<ConversationResponse> getOrCreateConversation(Long otherUserId) {
        long connectId = extractConnectId();
        log.info("Getting or creating conversation between {} and {}", connectId, otherUserId);

        Conversation conversation = conversationService.getOrCreateConversation(connectId, otherUserId);

        String otherUserName = "";
        String otherUserPhotoUrl = "";

        var profile = profileRepository.findByConnectId(otherUserId);
        if (profile.isPresent()) {
            otherUserName = profile.get().firstName() + " " + profile.get().lastName();
            var photos = profile.get().photoUrls();
            if (photos != null && !photos.isEmpty()) {
                otherUserPhotoUrl = photos.get(0);
            }
        }

        ConversationResponse response = conversationMapper.toResponse(
                conversation, connectId, otherUserName, otherUserPhotoUrl, 0);

        return ResponseEntity.ok(response);
    }

    private long extractConnectId() {
        String authHeader = httpServletRequest.getHeader(X_AUTHORISATION);
        String token = authHeader.replace("Bearer ", "");
        return jwtValidatorService.extractConnectId(token);
    }
}
