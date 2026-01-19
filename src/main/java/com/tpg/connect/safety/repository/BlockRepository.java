package com.tpg.connect.safety.repository;

import com.google.cloud.firestore.Firestore;
import com.tpg.connect.safety.mapper.BlockMapper;
import com.tpg.connect.safety.model.entity.Block;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class BlockRepository {

    private static final String COLLECTION_NAME = "Blocks";
    private final Firestore firestore;
    private final BlockMapper blockMapper;

    public Block save(Block block) {
        try {
            String blockId = block.blockId() != null ? block.blockId() : UUID.randomUUID().toString();
            Block blockToSave = Block.builder()
                    .blockId(blockId)
                    .blockerConnectId(block.blockerConnectId())
                    .blockedConnectId(block.blockedConnectId())
                    .createdAt(block.createdAt())
                    .build();

            Map<String, Object> data = blockMapper.toDocument(blockToSave);
            firestore.collection(COLLECTION_NAME).document(blockId).set(data).get();
            log.info("Block saved: {} blocked {}", block.blockerConnectId(), block.blockedConnectId());
            return blockToSave;
        } catch (Exception e) {
            log.error("Failed to save block", e);
            throw new RuntimeException("Failed to save block", e);
        }
    }

    public boolean delete(long blockerConnectId, long blockedConnectId) {
        try {
            var docs = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("blockerConnectId", blockerConnectId)
                    .whereEqualTo("blockedConnectId", blockedConnectId)
                    .get().get().getDocuments();
            for (var doc : docs) {
                doc.getReference().delete();
            }
            log.info("Block deleted: {} unblocked {}", blockerConnectId, blockedConnectId);
            return true;
        } catch (Exception e) {
            log.error("Failed to delete block", e);
            return false;
        }
    }

    public List<Long> findBlockedUserIds(long connectId) {
        try {
            var docs = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("blockerConnectId", connectId)
                    .get().get().getDocuments();
            return docs.stream()
                    .map(doc -> ((Number) doc.get("blockedConnectId")).longValue())
                    .toList();
        } catch (Exception e) {
            log.error("Failed to find blocked users for: {}", connectId, e);
            return List.of();
        }
    }

    public List<Long> findBlockedByUserIds(long connectId) {
        try {
            var docs = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("blockedConnectId", connectId)
                    .get().get().getDocuments();
            return docs.stream()
                    .map(doc -> ((Number) doc.get("blockerConnectId")).longValue())
                    .toList();
        } catch (Exception e) {
            log.error("Failed to find users who blocked: {}", connectId, e);
            return List.of();
        }
    }

    public boolean isBlocked(long blockerConnectId, long blockedConnectId) {
        try {
            var docs = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("blockerConnectId", blockerConnectId)
                    .whereEqualTo("blockedConnectId", blockedConnectId)
                    .get().get().getDocuments();
            return !docs.isEmpty();
        } catch (Exception e) {
            log.error("Failed to check block status", e);
            return false;
        }
    }
}
