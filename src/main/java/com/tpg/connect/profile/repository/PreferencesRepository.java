package com.tpg.connect.profile.repository;

import com.google.cloud.firestore.Firestore;
import com.tpg.connect.profile.mapper.PreferencesMapper;
import com.tpg.connect.profile.model.entity.UserPreferences;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PreferencesRepository {

    private static final String COLLECTION_NAME = "UserPreferences";
    private final Firestore firestore;
    private final PreferencesMapper preferencesMapper;

    public Optional<UserPreferences> findByConnectId(long connectId) {
        try {
            var doc = firestore.collection(COLLECTION_NAME)
                    .document(String.valueOf(connectId))
                    .get()
                    .get();

            if (!doc.exists()) {
                log.info("No preferences found for connectId: {}", connectId);
                return Optional.empty();
            }

            Map<String, Object> data = doc.getData();
            if (data == null) {
                return Optional.empty();
            }

            return Optional.of(preferencesMapper.toEntity(data, connectId));
        } catch (Exception e) {
            log.error("Failed to get preferences for connectId: {}", connectId, e);
            return Optional.empty();
        }
    }

    public boolean save(UserPreferences preferences) {
        try {
            Map<String, Object> data = preferencesMapper.toDocument(preferences);
            firestore.collection(COLLECTION_NAME)
                    .document(String.valueOf(preferences.connectId()))
                    .set(data)
                    .get();
            log.info("Preferences saved for connectId: {}", preferences.connectId());
            return true;
        } catch (Exception e) {
            log.error("Failed to save preferences for connectId: {}", preferences.connectId(), e);
            return false;
        }
    }

    public boolean delete(long connectId) {
        try {
            firestore.collection(COLLECTION_NAME)
                    .document(String.valueOf(connectId))
                    .delete()
                    .get();
            log.info("Preferences deleted for connectId: {}", connectId);
            return true;
        } catch (Exception e) {
            log.error("Failed to delete preferences for connectId: {}", connectId, e);
            return false;
        }
    }
}

