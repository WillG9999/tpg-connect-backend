package com.tpg.connect.conversation.repository;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.tpg.connect.conversation.mapper.MessageMapper;
import com.tpg.connect.conversation.model.entity.Message;
import com.tpg.connect.conversation.model.response.PaginatedResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MessageRepository {

    private static final String COLLECTION_NAME = "Conversations";
    private static final String MESSAGES_SUBCOLLECTION = "messages";

    private final Firestore firestore;
    private final MessageMapper messageMapper;

    public Message save(Message message) {
        try {
            String messageId = message.messageId() != null ? message.messageId() : UUID.randomUUID().toString();
            Message messageToSave = Message.builder()
                    .messageId(messageId)
                    .conversationId(message.conversationId())
                    .senderId(message.senderId())
                    .content(message.content())
                    .timestamp(message.timestamp())
                    .readAt(message.readAt())
                    .build();

            Map<String, Object> data = messageMapper.toDocument(messageToSave);
            firestore.collection(COLLECTION_NAME)
                    .document(message.conversationId())
                    .collection(MESSAGES_SUBCOLLECTION)
                    .document(messageId)
                    .set(data)
                    .get();

            log.info("Message saved: {} in conversation: {}", messageId, message.conversationId());
            return messageToSave;
        } catch (Exception e) {
            log.error("Failed to save message in conversation: {}", message.conversationId(), e);
            throw new RuntimeException("Failed to save message", e);
        }
    }

    public List<Message> findByConversationId(String conversationId, int limit, String beforeMessageId) {
        try {
            Query query = firestore.collection(COLLECTION_NAME)
                    .document(conversationId)
                    .collection(MESSAGES_SUBCOLLECTION)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(limit);

            var docs = query.get().get().getDocuments();

            List<Message> messages = new ArrayList<>();
            for (QueryDocumentSnapshot doc : docs) {
                messages.add(messageMapper.fromDocument(doc.getData(), doc.getId()));
            }
            return messages;
        } catch (Exception e) {
            log.error("Failed to find messages for conversation: {}", conversationId, e);
            return List.of();
        }
    }

    public PaginatedResult findByConversationIdWithCursor(String conversationId, int limit, String cursor) {
        try {
            Query query = firestore.collection(COLLECTION_NAME)
                    .document(conversationId)
                    .collection(MESSAGES_SUBCOLLECTION)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(limit + 1);

            if (cursor != null && !cursor.isEmpty()) {
                long cursorTimestamp = Long.parseLong(cursor);
                query = query.startAfter(cursorTimestamp);
            }

            var docs = query.get().get().getDocuments();

            List<Message> messages = new ArrayList<>();
            boolean hasMore = docs.size() > limit;

            int count = 0;
            for (QueryDocumentSnapshot doc : docs) {
                if (count >= limit) break;
                messages.add(messageMapper.fromDocument(doc.getData(), doc.getId()));
                count++;
            }

            String nextCursor = null;
            if (hasMore && !messages.isEmpty()) {
                nextCursor = String.valueOf(messages.getLast().timestamp().toEpochMilli());
            }

            return new PaginatedResult(messages, nextCursor, hasMore);
        } catch (Exception e) {
            log.error("Failed to find messages with cursor for conversation: {}", conversationId, e);
            return new PaginatedResult(List.of(), null, false);
        }
    }

    public int countUnreadMessages(String conversationId, long recipientId) {
        try {
            var docs = firestore.collection(COLLECTION_NAME)
                    .document(conversationId)
                    .collection(MESSAGES_SUBCOLLECTION)
                    .whereNotEqualTo("senderId", recipientId)
                    .whereEqualTo("readAt", null)
                    .get()
                    .get()
                    .getDocuments();

            return docs.size();
        } catch (Exception e) {
            log.error("Failed to count unread messages for conversation: {}", conversationId, e);
            return 0;
        }
    }

    public boolean markMessagesAsRead(String conversationId, long recipientId) {
        try {
            var docs = firestore.collection(COLLECTION_NAME)
                    .document(conversationId)
                    .collection(MESSAGES_SUBCOLLECTION)
                    .whereNotEqualTo("senderId", recipientId)
                    .whereEqualTo("readAt", null)
                    .get()
                    .get()
                    .getDocuments();

            long now = Instant.now().toEpochMilli();
            for (QueryDocumentSnapshot doc : docs) {
                doc.getReference().update("readAt", now);
            }

            log.info("Marked {} messages as read in conversation: {}", docs.size(), conversationId);
            return true;
        } catch (Exception e) {
            log.error("Failed to mark messages as read for conversation: {}", conversationId, e);
            return false;
        }
    }
}
