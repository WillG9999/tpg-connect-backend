package com.tpg.connect.settings.service;

import com.google.cloud.firestore.Firestore;
import com.tpg.connect.profile.model.entity.UserProfile;
import com.tpg.connect.profile.repository.ProfileRepository;
import com.tpg.connect.settings.model.response.AccountDataResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private static final String DEACTIVATED_USERS_COLLECTION = "DeactivatedUsers";

    private final Firestore firestore;
    private final ProfileRepository profileRepository;
    private final SettingsService settingsService;

    public boolean deactivateAccount(long connectId, String reason) {
        log.info("Deactivating account for connectId: {}", connectId);

        try {
            Optional<UserProfile> profileOpt = profileRepository.findByConnectId(connectId);

            Map<String, Object> deactivatedData = new HashMap<>();
            deactivatedData.put("connectId", connectId);
            deactivatedData.put("reason", reason);
            deactivatedData.put("deactivatedAt", Instant.now().toEpochMilli());
            deactivatedData.put("email", profileOpt.map(UserProfile::email).orElse(null));
            deactivatedData.put("status", "DEACTIVATED");

            firestore.collection(DEACTIVATED_USERS_COLLECTION)
                    .document(String.valueOf(connectId))
                    .set(deactivatedData)
                    .get();

            log.info("Account deactivated successfully for connectId: {}", connectId);
            return true;
        } catch (Exception e) {
            log.error("Failed to deactivate account for connectId: {}", connectId, e);
            return false;
        }
    }

    public AccountDataResponse downloadAccountData(long connectId) {
        log.info("Downloading account data for connectId: {}", connectId);

        try {
            Optional<UserProfile> profile = profileRepository.findByConnectId(connectId);
            var notificationSettings = settingsService.getNotificationSettings(connectId);
            var emailSettings = settingsService.getEmailSettings(connectId);
            var privacySettings = settingsService.getPrivacySettings(connectId);

            return new AccountDataResponse(
                    true,
                    profile.orElse(null),
                    notificationSettings.settings(),
                    emailSettings.settings(),
                    privacySettings.settings(),
                    Instant.now()
            );
        } catch (Exception e) {
            log.error("Failed to download account data for connectId: {}", connectId, e);
            return new AccountDataResponse(false, null, null, null, null, null);
        }
    }

    public boolean reactivateAccount(long connectId) {
        log.info("Reactivating account for connectId: {}", connectId);

        try {
            firestore.collection(DEACTIVATED_USERS_COLLECTION)
                    .document(String.valueOf(connectId))
                    .delete()
                    .get();

            log.info("Account reactivated successfully for connectId: {}", connectId);
            return true;
        } catch (Exception e) {
            log.error("Failed to reactivate account for connectId: {}", connectId, e);
            return false;
        }
    }

    public boolean isAccountDeactivated(long connectId) {
        try {
            var doc = firestore.collection(DEACTIVATED_USERS_COLLECTION)
                    .document(String.valueOf(connectId))
                    .get()
                    .get();
            return doc.exists();
        } catch (Exception e) {
            log.error("Failed to check if account is deactivated: {}", connectId, e);
            return false;
        }
    }
}

