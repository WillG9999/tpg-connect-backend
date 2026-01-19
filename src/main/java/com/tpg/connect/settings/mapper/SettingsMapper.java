package com.tpg.connect.settings.mapper;

import com.tpg.connect.settings.model.entity.EmailSettings;
import com.tpg.connect.settings.model.entity.NotificationSettings;
import com.tpg.connect.settings.model.entity.PrivacySettings;
import com.tpg.connect.settings.model.entity.UserNotification;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
public class SettingsMapper {

    public Map<String, Object> notificationSettingsToDocument(NotificationSettings settings) {
        Map<String, Object> data = new HashMap<>();
        data.put("connectId", settings.connectId());
        data.put("pushEnabled", settings.pushEnabled());
        data.put("newMatchNotification", settings.newMatchNotification());
        data.put("newMessageNotification", settings.newMessageNotification());
        data.put("newLikeNotification", settings.newLikeNotification());
        data.put("dailyBatchNotification", settings.dailyBatchNotification());
        data.put("promotionalNotification", settings.promotionalNotification());
        return data;
    }

    public NotificationSettings documentToNotificationSettings(Map<String, Object> data, long connectId) {
        return NotificationSettings.builder()
                .connectId(connectId)
                .pushEnabled(getBoolean(data, "pushEnabled", true))
                .newMatchNotification(getBoolean(data, "newMatchNotification", true))
                .newMessageNotification(getBoolean(data, "newMessageNotification", true))
                .newLikeNotification(getBoolean(data, "newLikeNotification", true))
                .dailyBatchNotification(getBoolean(data, "dailyBatchNotification", true))
                .promotionalNotification(getBoolean(data, "promotionalNotification", false))
                .build();
    }

    public Map<String, Object> emailSettingsToDocument(EmailSettings settings) {
        Map<String, Object> data = new HashMap<>();
        data.put("connectId", settings.connectId());
        data.put("emailEnabled", settings.emailEnabled());
        data.put("newMatchEmail", settings.newMatchEmail());
        data.put("newMessageEmail", settings.newMessageEmail());
        data.put("weeklyDigestEmail", settings.weeklyDigestEmail());
        data.put("promotionalEmail", settings.promotionalEmail());
        data.put("securityAlertEmail", settings.securityAlertEmail());
        return data;
    }

    public EmailSettings documentToEmailSettings(Map<String, Object> data, long connectId) {
        return EmailSettings.builder()
                .connectId(connectId)
                .emailEnabled(getBoolean(data, "emailEnabled", true))
                .newMatchEmail(getBoolean(data, "newMatchEmail", true))
                .newMessageEmail(getBoolean(data, "newMessageEmail", false))
                .weeklyDigestEmail(getBoolean(data, "weeklyDigestEmail", true))
                .promotionalEmail(getBoolean(data, "promotionalEmail", false))
                .securityAlertEmail(getBoolean(data, "securityAlertEmail", true))
                .build();
    }

    public Map<String, Object> privacySettingsToDocument(PrivacySettings settings) {
        Map<String, Object> data = new HashMap<>();
        data.put("connectId", settings.connectId());
        data.put("showOnlineStatus", settings.showOnlineStatus());
        data.put("showLastActive", settings.showLastActive());
        data.put("showReadReceipts", settings.showReadReceipts());
        data.put("showProfileInSearch", settings.showProfileInSearch());
        data.put("allowScreenshots", settings.allowScreenshots());
        data.put("hideFromBlockedContacts", settings.hideFromBlockedContacts());
        return data;
    }

    public PrivacySettings documentToPrivacySettings(Map<String, Object> data, long connectId) {
        return PrivacySettings.builder()
                .connectId(connectId)
                .showOnlineStatus(getBoolean(data, "showOnlineStatus", true))
                .showLastActive(getBoolean(data, "showLastActive", true))
                .showReadReceipts(getBoolean(data, "showReadReceipts", true))
                .showProfileInSearch(getBoolean(data, "showProfileInSearch", true))
                .allowScreenshots(getBoolean(data, "allowScreenshots", true))
                .hideFromBlockedContacts(getBoolean(data, "hideFromBlockedContacts", true))
                .build();
    }

    public Map<String, Object> userNotificationToDocument(UserNotification notification) {
        Map<String, Object> data = new HashMap<>();
        data.put("notificationId", notification.notificationId());
        data.put("connectId", notification.connectId());
        data.put("type", notification.type().name());
        data.put("title", notification.title());
        data.put("message", notification.message());
        data.put("actionUrl", notification.actionUrl());
        data.put("relatedConnectId", notification.relatedConnectId());
        data.put("createdAt", notification.createdAt() != null ? notification.createdAt().toEpochMilli() : null);
        data.put("read", notification.read());
        return data;
    }

    public UserNotification documentToUserNotification(Map<String, Object> data, String notificationId) {
        return UserNotification.builder()
                .notificationId(notificationId)
                .connectId(getLong(data, "connectId", 0L))
                .type(UserNotification.NotificationType.valueOf(getString(data, "type", "SYSTEM_ANNOUNCEMENT")))
                .title(getString(data, "title", ""))
                .message(getString(data, "message", ""))
                .actionUrl(getString(data, "actionUrl", null))
                .relatedConnectId(data.get("relatedConnectId") != null ? ((Number) data.get("relatedConnectId")).longValue() : null)
                .createdAt(data.get("createdAt") != null ? Instant.ofEpochMilli(((Number) data.get("createdAt")).longValue()) : Instant.now())
                .read(getBoolean(data, "read", false))
                .build();
    }

    private boolean getBoolean(Map<String, Object> data, String key, boolean defaultValue) {
        Object value = data.get(key);
        return value != null ? (Boolean) value : defaultValue;
    }

    private String getString(Map<String, Object> data, String key, String defaultValue) {
        Object value = data.get(key);
        return value != null ? (String) value : defaultValue;
    }

    private long getLong(Map<String, Object> data, String key, long defaultValue) {
        Object value = data.get(key);
        return value != null ? ((Number) value).longValue() : defaultValue;
    }
}

