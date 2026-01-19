package com.tpg.connect.profile.repository;

import com.google.cloud.firestore.Firestore;
import com.tpg.connect.profile.mapper.ProfileMapper;
import com.tpg.connect.profile.model.entity.UserProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ProfileRepository {

    private static final String PROFILES_COLLECTION = "Profiles";
    private final Firestore firestore;
    private final ProfileMapper profileMapper;

    public boolean createProfile(UserProfile profile) {
        try {
            Map<String, Object> data = profileMapper.profileToDocument(profile);
            firestore.collection(PROFILES_COLLECTION)
                    .document(String.valueOf(profile.connectId()))
                    .set(data)
                    .get();
            log.info("Profile created for connectId: {}", profile.connectId());
            return true;
        } catch (Exception e) {
            log.error("Failed to create profile for connectId: {}", profile.connectId(), e);
            return false;
        }
    }

    public Optional<UserProfile> findByConnectId(long connectId) {
        try {
            var doc = firestore.collection(PROFILES_COLLECTION)
                    .document(String.valueOf(connectId))
                    .get()
                    .get();
            if (!doc.exists()) {
                return Optional.empty();
            }
            return Optional.of(profileMapper.documentToProfile(doc.getData(), connectId));
        } catch (Exception e) {
            log.error("Failed to find profile for connectId: {}", connectId, e);
            return Optional.empty();
        }
    }

    public boolean updateProfile(long connectId, Map<String, Object> updates) {
        try {
            firestore.collection(PROFILES_COLLECTION)
                    .document(String.valueOf(connectId))
                    .update(updates)
                    .get();
            log.info("Profile updated for connectId: {}", connectId);
            return true;
        } catch (Exception e) {
            log.error("Failed to update profile for connectId: {}", connectId, e);
            return false;
        }
    }
}
