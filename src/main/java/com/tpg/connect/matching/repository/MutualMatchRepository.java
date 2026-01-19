package com.tpg.connect.matching.repository;

import com.google.cloud.firestore.Firestore;
import com.tpg.connect.matching.mapper.MatchingMapper;
import com.tpg.connect.matching.model.entity.MutualMatch;
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
public class MutualMatchRepository {

    private static final String COLLECTION_NAME = "MutualMatches";
    private final Firestore firestore;
    private final MatchingMapper matchingMapper;

    public MutualMatch save(MutualMatch match) {
        try {
            String matchId = match.matchId();
            Map<String, Object> data = matchingMapper.mutualMatchToDocument(match);
            firestore.collection(COLLECTION_NAME).document(matchId).set(data).get();
            log.info("Mutual match saved: {} <-> {}", match.connectId1(), match.connectId2());
            return match;
        } catch (Exception e) {
            log.error("Failed to save mutual match", e);
            throw new RuntimeException("Failed to save mutual match", e);
        }
    }

    public List<MutualMatch> findByConnectId(long connectId) {
        try {
            List<MutualMatch> matches = new ArrayList<>();

            var docs1 = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("connectId1", connectId)
                    .get()
                    .get()
                    .getDocuments();

            for (var doc : docs1) {
                matches.add(matchingMapper.documentToMutualMatch(doc.getData(), doc.getId()));
            }

            var docs2 = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("connectId2", connectId)
                    .get()
                    .get()
                    .getDocuments();

            for (var doc : docs2) {
                matches.add(matchingMapper.documentToMutualMatch(doc.getData(), doc.getId()));
            }

            return matches;
        } catch (Exception e) {
            log.error("Failed to find matches for connectId: {}", connectId, e);
            return List.of();
        }
    }

    public Optional<MutualMatch> findByPair(long connectId1, long connectId2) {
        try {
            long lower = Math.min(connectId1, connectId2);
            long higher = Math.max(connectId1, connectId2);

            var docs = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("connectId1", lower)
                    .whereEqualTo("connectId2", higher)
                    .get()
                    .get()
                    .getDocuments();

            if (docs.isEmpty()) {
                return Optional.empty();
            }

            var doc = docs.get(0);
            return Optional.of(matchingMapper.documentToMutualMatch(doc.getData(), doc.getId()));
        } catch (Exception e) {
            log.error("Failed to find match by pair", e);
            return Optional.empty();
        }
    }

    public boolean exists(long connectId1, long connectId2) {
        return findByPair(connectId1, connectId2).isPresent();
    }
}

