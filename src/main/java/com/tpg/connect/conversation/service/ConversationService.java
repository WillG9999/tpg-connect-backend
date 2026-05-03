package com.tpg.connect.conversation.service;

import com.tpg.connect.conversation.mapper.ConversationMapper;
import com.tpg.connect.conversation.mapper.MessageMapper;
import com.tpg.connect.conversation.model.entity.Conversation;
import com.tpg.connect.conversation.model.entity.Message;
import com.tpg.connect.conversation.model.response.ConversationResponse;
import com.tpg.connect.conversation.model.response.MessageResponse;
import com.tpg.connect.conversation.model.response.PaginatedMessagesResponse;
import com.tpg.connect.conversation.repository.ConversationRepository;
import com.tpg.connect.conversation.repository.MessageRepository;
import com.tpg.connect.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ProfileRepository profileRepository;
    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;

    public List<ConversationResponse> getConversationsForUser(long connectId) {
        log.info("Getting conversations for user: {}", connectId);

        List<Conversation> conversations = conversationRepository.findByParticipant(connectId);
        List<ConversationResponse> responses = new ArrayList<>();

        for (Conversation conversation : conversations) {
            Long otherUserId = conversation.participants().stream()
                    .filter(id -> id != connectId)
                    .findFirst()
                    .orElse(null);

            String otherUserName = "";
            String otherUserPhotoUrl = "";

            if (otherUserId != null) {
                var profile = profileRepository.findByConnectId(otherUserId);
                if (profile.isPresent()) {
                    otherUserName = profile.get().firstName() + " " + profile.get().lastName();
                    var photos = profile.get().photoUrls();
                    if (photos != null && !photos.isEmpty()) {
                        otherUserPhotoUrl = photos.get(0);
                    }
                }
            }

            int unreadCount = messageRepository.countUnreadMessages(conversation.conversationId(), connectId);

            responses.add(conversationMapper.toResponse(conversation, connectId, otherUserName, otherUserPhotoUrl, unreadCount));
        }

        return responses;
    }

    public Optional<Conversation> getConversation(String conversationId) {
        return conversationRepository.findById(conversationId);
    }

    public Conversation getOrCreateConversation(long connectId1, long connectId2) {
        String conversationId = Conversation.generateConversationId(connectId1, connectId2);
        log.info("Getting or creating conversation: {}", conversationId);

        Optional<Conversation> existing = conversationRepository.findById(conversationId);
        if (existing.isPresent()) {
            return existing.get();
        }

        Conversation newConversation = Conversation.builder()
                .conversationId(conversationId)
                .participants(List.of(Math.min(connectId1, connectId2), Math.max(connectId1, connectId2)))
                .createdAt(Instant.now())
                .build();

        conversationRepository.save(newConversation);
        return newConversation;
    }

    public MessageResponse sendMessage(String conversationId, long senderId, String content) {
        log.info("Sending message in conversation: {} from user: {}", conversationId, senderId);

        Optional<Conversation> conversationOpt = conversationRepository.findById(conversationId);
        if (conversationOpt.isEmpty()) {
            log.error("Conversation not found: {}", conversationId);
            throw new IllegalArgumentException("Conversation not found");
        }

        Conversation conversation = conversationOpt.get();
        if (!conversation.participants().contains(senderId)) {
            log.error("User {} is not a participant in conversation {}", senderId, conversationId);
            throw new IllegalArgumentException("User is not a participant in this conversation");
        }

        Instant now = Instant.now();
        Message message = Message.builder()
                .conversationId(conversationId)
                .senderId(senderId)
                .content(content)
                .timestamp(now)
                .build();

        Message savedMessage = messageRepository.save(message);
        conversationRepository.updateLastMessage(conversationId, content, senderId, now.toEpochMilli());

        return messageMapper.toResponse(savedMessage);
    }

    public List<MessageResponse> getMessages(String conversationId, long connectId, int limit) {
        log.info("Getting messages for conversation: {}", conversationId);

        Optional<Conversation> conversationOpt = conversationRepository.findById(conversationId);
        if (conversationOpt.isEmpty()) {
            return List.of();
        }

        if (!conversationOpt.get().participants().contains(connectId)) {
            log.warn("User {} is not a participant in conversation {}", connectId, conversationId);
            return List.of();
        }

        List<Message> messages = messageRepository.findByConversationId(conversationId, limit, null);
        return messages.stream()
                .map(messageMapper::toResponse)
                .toList();
    }

    public PaginatedMessagesResponse getMessagesWithCursor(String conversationId, long connectId, int limit, String cursor) {
        log.info("Getting messages for conversation: {} with cursor: {}", conversationId, cursor);

        Optional<Conversation> conversationOpt = conversationRepository.findById(conversationId);
        if (conversationOpt.isEmpty() || !conversationOpt.get().participants().contains(connectId)) {
            return new PaginatedMessagesResponse(List.of(), null, false);
        }

        var result = messageRepository.findByConversationIdWithCursor(conversationId, limit, cursor);

        List<MessageResponse> responses = result.messages().stream()
                .map(messageMapper::toResponse)
                .toList();

        return new PaginatedMessagesResponse(responses, result.nextCursor(), result.hasMore());
    }

    public boolean markAsRead(String conversationId, long connectId) {
        log.info("Marking messages as read in conversation: {} for user: {}", conversationId, connectId);

        Optional<Conversation> conversationOpt = conversationRepository.findById(conversationId);
        if (conversationOpt.isEmpty()) {
            return false;
        }

        if (!conversationOpt.get().participants().contains(connectId)) {
            return false;
        }

        return messageRepository.markMessagesAsRead(conversationId, connectId);
    }

    public boolean archiveConversation(String conversationId, long connectId) {
        log.info("Archiving conversation: {} for user: {}", conversationId, connectId);
        return setArchivedState(conversationId, connectId, true);
    }

    public boolean unarchiveConversation(String conversationId, long connectId) {
        log.info("Unarchiving conversation: {} for user: {}", conversationId, connectId);
        return setArchivedState(conversationId, connectId, false);
    }

    private boolean setArchivedState(String conversationId, long connectId, boolean archived) {
        Optional<Conversation> conversationOpt = conversationRepository.findById(conversationId);
        if (conversationOpt.isEmpty()) return false;
        if (!conversationOpt.get().participants().contains(connectId)) return false;
        return conversationRepository.setArchived(conversationId, archived);
    }
}
