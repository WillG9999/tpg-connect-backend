package com.tpg.connect.safety.repository;

import com.google.cloud.firestore.Firestore;
import com.tpg.connect.safety.mapper.BlockedUserMapper;
import com.tpg.connect.safety.model.entity.BlockedUser;
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
public class BlockedUserRepository {

    private static final String COLLECTION_NAME = "BlockedUsers";
    private final Firestore firestore;
    private final BlockedUserMapper blockedUserMapper;

    public List<BlockedUser> findByBlockerConnectId(long blockerConnectId) {
        try {
            var docs = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("blockerConnectId", blockerConnectId)
                    .get()
                    .get()
                    .getDocuments();

            List<BlockedUser> blockedUsers = new ArrayList<>();
            for (var doc : docs) {
                blockedUsers.add(blockedUserMapper.toEntity(doc.getData(), doc.getId()));
            }
            log.info("Found {} blocked users for connectId: {}", blockedUsers.size(), blockerConnectId);
            return blockedUsers;
        } catch (Exception e) {
            log.error("Failed to get blocked users for connectId: {}", blockerConnectId, e);
            return List.of();
        }
    }

    public Optional<BlockedUser> findByBlockerAndBlocked(long blockerConnectId, long blockedConnectId) {
        try {
            var docs = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("blockerConnectId", blockerConnectId)
                    .whereEqualTo("blockedConnectId", blockedConnectId)
                    .get()
                    .get()
                    .getDocuments();

            if (docs.isEmpty()) {
                return Optional.empty();
            }
            var doc = docs.get(0);
            return Optional.of(blockedUserMapper.toEntity(doc.getData(), doc.getId()));
        } catch (Exception e) {
            log.error("Failed to find blocked user", e);
            return Optional.empty();
        }
    }

    public boolean save(BlockedUser blockedUser) {
        try {
            Map<String, Object> data = blockedUserMapper.toDocument(blockedUser);
            firestore.collection(COLLECTION_NAME)
                    .document(blockedUser.blockId())
                    .set(data)
                    .get();
            log.info("Blocked user saved: {} blocked {}", blockedUser.blockerConnectId(), blockedUser.blockedConnectId());
            return true;
        } catch (Exception e) {
            log.error("Failed to save blocked user", e);
            return false;
        }
    }

    public boolean delete(String blockId) {
        try {
            firestore.collection(COLLECTION_NAME)
                    .document(blockId)
                    .delete()
                    .get();
            log.info("Blocked user deleted: {}", blockId);
            return true;
        } catch (Exception e) {
            log.error("Failed to delete blocked user: {}", blockId, e);
            return false;
        }
    }

    public boolean isBlocked(long blockerConnectId, long blockedConnectId) {
        return findByBlockerAndBlocked(blockerConnectId, blockedConnectId).isPresent();
    }

    public int countBlockedUsers(long blockerConnectId) {
        try {
            var docs = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("blockerConnectId", blockerConnectId)
                    .get()
                    .get()
                    .getDocuments();
            return docs.size();
        } catch (Exception e) {
            log.error("Failed to count blocked users", e);
            return 0;
        }
    }
}

