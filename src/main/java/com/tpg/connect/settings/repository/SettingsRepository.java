package com.tpg.connect.settings.repository;

import com.google.cloud.firestore.Firestore;
import com.tpg.connect.settings.mapper.SettingsMapper;
import com.tpg.connect.settings.model.entity.EmailSettings;
import com.tpg.connect.settings.model.entity.NotificationSettings;
import com.tpg.connect.settings.model.entity.PrivacySettings;
import com.tpg.connect.settings.model.entity.UserNotification;
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
public class SettingsRepository {

    private static final String NOTIFICATION_SETTINGS_COLLECTION = "NotificationSettings";
    private static final String EMAIL_SETTINGS_COLLECTION = "EmailSettings";
    private static final String PRIVACY_SETTINGS_COLLECTION = "PrivacySettings";
    private static final String USER_NOTIFICATIONS_COLLECTION = "UserNotifications";

    private final Firestore firestore;
    private final SettingsMapper settingsMapper;

    public Optional<NotificationSettings> findNotificationSettings(long connectId) {
        try {
            var doc = firestore.collection(NOTIFICATION_SETTINGS_COLLECTION)
                    .document(String.valueOf(connectId))
                    .get()
                    .get();

            if (!doc.exists() || doc.getData() == null) {
                return Optional.empty();
            }
            return Optional.of(settingsMapper.documentToNotificationSettings(doc.getData(), connectId));
        } catch (Exception e) {
            log.error("Failed to get notification settings for: {}", connectId, e);
            return Optional.empty();
        }
    }

    public boolean saveNotificationSettings(NotificationSettings settings) {
        try {
            Map<String, Object> data = settingsMapper.notificationSettingsToDocument(settings);
            firestore.collection(NOTIFICATION_SETTINGS_COLLECTION)
                    .document(String.valueOf(settings.connectId()))
                    .set(data)
                    .get();
            log.info("Notification settings saved for: {}", settings.connectId());
            return true;
        } catch (Exception e) {
            log.error("Failed to save notification settings", e);
            return false;
        }
    }

    public Optional<EmailSettings> findEmailSettings(long connectId) {
        try {
            var doc = firestore.collection(EMAIL_SETTINGS_COLLECTION)
                    .document(String.valueOf(connectId))
                    .get()
                    .get();

            if (!doc.exists() || doc.getData() == null) {
                return Optional.empty();
            }
            return Optional.of(settingsMapper.documentToEmailSettings(doc.getData(), connectId));
        } catch (Exception e) {
            log.error("Failed to get email settings for: {}", connectId, e);
            return Optional.empty();
        }
    }

    public boolean saveEmailSettings(EmailSettings settings) {
        try {
            Map<String, Object> data = settingsMapper.emailSettingsToDocument(settings);
            firestore.collection(EMAIL_SETTINGS_COLLECTION)
                    .document(String.valueOf(settings.connectId()))
                    .set(data)
                    .get();
            log.info("Email settings saved for: {}", settings.connectId());
            return true;
        } catch (Exception e) {
            log.error("Failed to save email settings", e);
            return false;
        }
    }

    public Optional<PrivacySettings> findPrivacySettings(long connectId) {
        try {
            var doc = firestore.collection(PRIVACY_SETTINGS_COLLECTION)
                    .document(String.valueOf(connectId))
                    .get()
                    .get();

            if (!doc.exists() || doc.getData() == null) {
                return Optional.empty();
            }
            return Optional.of(settingsMapper.documentToPrivacySettings(doc.getData(), connectId));
        } catch (Exception e) {
            log.error("Failed to get privacy settings for: {}", connectId, e);
            return Optional.empty();
        }
    }

    public boolean savePrivacySettings(PrivacySettings settings) {
        try {
            Map<String, Object> data = settingsMapper.privacySettingsToDocument(settings);
            firestore.collection(PRIVACY_SETTINGS_COLLECTION)
                    .document(String.valueOf(settings.connectId()))
                    .set(data)
                    .get();
            log.info("Privacy settings saved for: {}", settings.connectId());
            return true;
        } catch (Exception e) {
            log.error("Failed to save privacy settings", e);
            return false;
        }
    }

    public List<UserNotification> findNotificationsByConnectId(long connectId, int limit) {
        try {
            var docs = firestore.collection(USER_NOTIFICATIONS_COLLECTION)
                    .whereEqualTo("connectId", connectId)
                    .orderBy("createdAt", com.google.cloud.firestore.Query.Direction.DESCENDING)
                    .limit(limit)
                    .get()
                    .get()
                    .getDocuments();

            List<UserNotification> notifications = new ArrayList<>();
            for (var doc : docs) {
                notifications.add(settingsMapper.documentToUserNotification(doc.getData(), doc.getId()));
            }
            return notifications;
        } catch (Exception e) {
            log.error("Failed to get notifications for: {}", connectId, e);
            return List.of();
        }
    }

    public boolean saveNotification(UserNotification notification) {
        try {
            Map<String, Object> data = settingsMapper.userNotificationToDocument(notification);
            firestore.collection(USER_NOTIFICATIONS_COLLECTION)
                    .document(notification.notificationId())
                    .set(data)
                    .get();
            return true;
        } catch (Exception e) {
            log.error("Failed to save notification", e);
            return false;
        }
    }

    public boolean markNotificationAsRead(String notificationId) {
        try {
            firestore.collection(USER_NOTIFICATIONS_COLLECTION)
                    .document(notificationId)
                    .update("read", true)
                    .get();
            return true;
        } catch (Exception e) {
            log.error("Failed to mark notification as read: {}", notificationId, e);
            return false;
        }
    }

    public boolean markAllNotificationsAsRead(long connectId) {
        try {
            var docs = firestore.collection(USER_NOTIFICATIONS_COLLECTION)
                    .whereEqualTo("connectId", connectId)
                    .whereEqualTo("read", false)
                    .get()
                    .get()
                    .getDocuments();

            for (var doc : docs) {
                firestore.collection(USER_NOTIFICATIONS_COLLECTION)
                        .document(doc.getId())
                        .update("read", true)
                        .get();
            }
            log.info("Marked {} notifications as read for: {}", docs.size(), connectId);
            return true;
        } catch (Exception e) {
            log.error("Failed to mark all notifications as read for: {}", connectId, e);
            return false;
        }
    }

    public boolean deleteAllNotifications(long connectId) {
        try {
            var docs = firestore.collection(USER_NOTIFICATIONS_COLLECTION)
                    .whereEqualTo("connectId", connectId)
                    .get()
                    .get()
                    .getDocuments();

            for (var doc : docs) {
                firestore.collection(USER_NOTIFICATIONS_COLLECTION)
                        .document(doc.getId())
                        .delete()
                        .get();
            }
            log.info("Deleted {} notifications for: {}", docs.size(), connectId);
            return true;
        } catch (Exception e) {
            log.error("Failed to delete notifications for: {}", connectId, e);
            return false;
        }
    }

    public int countUnreadNotifications(long connectId) {
        try {
            var docs = firestore.collection(USER_NOTIFICATIONS_COLLECTION)
                    .whereEqualTo("connectId", connectId)
                    .whereEqualTo("read", false)
                    .get()
                    .get()
                    .getDocuments();
            return docs.size();
        } catch (Exception e) {
            log.error("Failed to count unread notifications for: {}", connectId, e);
            return 0;
        }
    }
}

