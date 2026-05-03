package com.tpg.connect.conversation.repository;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.tpg.connect.conversation.mapper.ConversationMapper;
import com.tpg.connect.conversation.model.entity.Conversation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ConversationRepository {

    private static final String COLLECTION_NAME = "Conversations";

    private final Firestore firestore;
    private final ConversationMapper conversationMapper;

    public Optional<Conversation> findById(String conversationId) {
        try {
            var doc = firestore.collection(COLLECTION_NAME)
                    .document(conversationId)
                    .get()
                    .get();

            if (!doc.exists()) {
                return Optional.empty();
            }

            return Optional.of(conversationMapper.fromDocument(doc.getData(), conversationId));
        } catch (Exception e) {
            log.error("Failed to find conversation: {}", conversationId, e);
            return Optional.empty();
        }
    }

    public List<Conversation> findByParticipant(long connectId) {
        try {
            var docs = firestore.collection(COLLECTION_NAME)
                    .whereArrayContains("participants", connectId)
                    .get()
                    .get()
                    .getDocuments();

            List<Conversation> conversations = new ArrayList<>();
            for (QueryDocumentSnapshot doc : docs) {
                conversations.add(conversationMapper.fromDocument(doc.getData(), doc.getId()));
            }
            return conversations;
        } catch (Exception e) {
            log.error("Failed to find conversations for user: {}", connectId, e);
            return List.of();
        }
    }

    public boolean save(Conversation conversation) {
        try {
            Map<String, Object> data = conversationMapper.toDocument(conversation);
            firestore.collection(COLLECTION_NAME)
                    .document(conversation.conversationId())
                    .set(data)
                    .get();
            log.info("Conversation saved: {}", conversation.conversationId());
            return true;
        } catch (Exception e) {
            log.error("Failed to save conversation: {}", conversation.conversationId(), e);
            return false;
        }
    }

    public boolean updateLastMessage(String conversationId, String content, Long senderId, long timestamp) {
        try {
            firestore.collection(COLLECTION_NAME)
                    .document(conversationId)
                    .update(
                            "lastMessageContent", content,
                            "lastMessageSenderId", senderId,
                            "lastMessageAt", timestamp
                    )
                    .get();
            return true;
        } catch (Exception e) {
            log.error("Failed to update last message for conversation: {}", conversationId, e);
            return false;
        }
    }

    public boolean setArchived(String conversationId, boolean archived) {
        try {
            firestore.collection(COLLECTION_NAME)
                    .document(conversationId)
                    .update("archived", archived)
                    .get();
            log.info("Conversation archived state updated - conversationId:: {} archived:: {}", conversationId, archived);
            return true;
        } catch (Exception e) {
            log.error("Failed to update archived state for conversation: {}", conversationId, e);
            return false;
        }
    }

    public boolean delete(String conversationId) {
        try {
            firestore.collection(COLLECTION_NAME).document(conversationId).delete().get();
            log.info("Conversation deleted: {}", conversationId);
            return true;
        } catch (Exception e) {
            log.error("Failed to delete conversation: {}", conversationId, e);
            return false;
        }
    }
}
