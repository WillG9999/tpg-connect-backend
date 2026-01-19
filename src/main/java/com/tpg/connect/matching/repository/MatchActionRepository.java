package com.tpg.connect.matching.repository;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.tpg.connect.matching.mapper.MatchingMapper;
import com.tpg.connect.matching.model.entity.MatchAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MatchActionRepository {

    private static final String COLLECTION_NAME = "MatchActions";
    private final Firestore firestore;
    private final MatchingMapper matchingMapper;

    public MatchAction save(MatchAction action) {
        try {
            String actionId = action.actionId() != null ? action.actionId() : UUID.randomUUID().toString();
            MatchAction actionToSave = MatchAction.builder()
                    .actionId(actionId)
                    .actorConnectId(action.actorConnectId())
                    .targetConnectId(action.targetConnectId())
                    .action(action.action())
                    .date(action.date() != null ? action.date() : LocalDate.now().toString())
                    .createdAt(action.createdAt() != null ? action.createdAt() : Instant.now())
                    .processed(action.processed())
                    .build();

            Map<String, Object> data = matchingMapper.matchActionToDocument(actionToSave);
            firestore.collection(COLLECTION_NAME).document(actionId).set(data).get();
            log.info("Match action saved: {} {} {}", actionToSave.actorConnectId(), actionToSave.action(), actionToSave.targetConnectId());
            return actionToSave;
        } catch (Exception e) {
            log.error("Failed to save match action", e);
            throw new RuntimeException("Failed to save match action", e);
        }
    }

    public List<MatchAction> saveAll(List<MatchAction> actions) {
        List<MatchAction> saved = new ArrayList<>();
        for (MatchAction action : actions) {
            saved.add(save(action));
        }
        return saved;
    }

    public boolean hasLiked(long actorConnectId, long targetConnectId) {
        try {
            var docs = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("actorConnectId", actorConnectId)
                    .whereEqualTo("targetConnectId", targetConnectId)
                    .whereEqualTo("action", "LIKE")
                    .get()
                    .get()
                    .getDocuments();
            return !docs.isEmpty();
        } catch (Exception e) {
            log.error("Failed to check if liked", e);
            return false;
        }
    }

    public Optional<MatchAction> findLike(long actorConnectId, long targetConnectId) {
        try {
            var docs = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("actorConnectId", actorConnectId)
                    .whereEqualTo("targetConnectId", targetConnectId)
                    .whereEqualTo("action", "LIKE")
                    .get()
                    .get()
                    .getDocuments();

            if (docs.isEmpty()) {
                return Optional.empty();
            }

            var doc = docs.get(0);
            return Optional.of(matchingMapper.documentToMatchAction(doc.getData(), doc.getId()));
        } catch (Exception e) {
            log.error("Failed to find like", e);
            return Optional.empty();
        }
    }

    public List<MatchAction> findUnprocessedLikes() {
        try {
            var docs = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("action", "LIKE")
                    .whereEqualTo("processed", false)
                    .get()
                    .get()
                    .getDocuments();

            List<MatchAction> actions = new ArrayList<>();
            for (var doc : docs) {
                actions.add(matchingMapper.documentToMatchAction(doc.getData(), doc.getId()));
            }
            return actions;
        } catch (Exception e) {
            log.error("Failed to find unprocessed likes", e);
            return List.of();
        }
    }

    public boolean markAsProcessed(String actionId) {
        try {
            firestore.collection(COLLECTION_NAME)
                    .document(actionId)
                    .update("processed", true)
                    .get();
            return true;
        } catch (Exception e) {
            log.error("Failed to mark action as processed: {}", actionId, e);
            return false;
        }
    }

    public List<Long> findActedOnToday(long actorConnectId) {
        try {
            String today = LocalDate.now().toString();
            var docs = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("actorConnectId", actorConnectId)
                    .whereEqualTo("date", today)
                    .get()
                    .get()
                    .getDocuments();

            List<Long> targetIds = new ArrayList<>();
            for (var doc : docs) {
                targetIds.add(((Number) doc.get("targetConnectId")).longValue());
            }
            return targetIds;
        } catch (Exception e) {
            log.error("Failed to find actions for today", e);
            return List.of();
        }
    }

    public List<MatchAction> findLikesReceivedBy(long targetConnectId) {
        try {
            var docs = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("targetConnectId", targetConnectId)
                    .whereEqualTo("action", "LIKE")
                    .whereEqualTo("processed", false)
                    .get()
                    .get()
                    .getDocuments();

            List<MatchAction> likes = new ArrayList<>();
            for (var doc : docs) {
                likes.add(matchingMapper.documentToMatchAction(doc.getData(), doc.getId()));
            }
            log.info("Found {} pending likes for user: {}", likes.size(), targetConnectId);
            return likes;
        } catch (Exception e) {
            log.error("Failed to find likes received by: {}", targetConnectId, e);
            return List.of();
        }
    }

    public boolean markLikeAsActioned(long actorConnectId, long targetConnectId, String status) {
        try {
            var docs = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("actorConnectId", actorConnectId)
                    .whereEqualTo("targetConnectId", targetConnectId)
                    .whereEqualTo("action", "LIKE")
                    .get()
                    .get()
                    .getDocuments();

            if (docs.isEmpty()) {
                return false;
            }

            for (var doc : docs) {
                firestore.collection(COLLECTION_NAME)
                        .document(doc.getId())
                        .update("processed", true, "likeResponse", status)
                        .get();
            }
            return true;
        } catch (Exception e) {
            log.error("Failed to mark like as actioned", e);
            return false;
        }
    }
}
