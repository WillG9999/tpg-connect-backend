package com.tpg.connect.settings.service;

import com.tpg.connect.settings.model.entity.EmailSettings;
import com.tpg.connect.settings.model.entity.NotificationSettings;
import com.tpg.connect.settings.model.entity.PrivacySettings;
import com.tpg.connect.settings.model.entity.UserNotification;
import com.tpg.connect.settings.model.request.UpdateEmailSettingsRequest;
import com.tpg.connect.settings.model.request.UpdateNotificationSettingsRequest;
import com.tpg.connect.settings.model.request.UpdatePrivacySettingsRequest;
import com.tpg.connect.settings.model.response.EmailSettingsResponse;
import com.tpg.connect.settings.model.response.NotificationSettingsResponse;
import com.tpg.connect.settings.model.response.NotificationsListResponse;
import com.tpg.connect.settings.model.response.PrivacySettingsResponse;
import com.tpg.connect.settings.repository.SettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettingsService {

    private final SettingsRepository settingsRepository;

    public NotificationSettingsResponse getNotificationSettings(long connectId) {
        log.info("Getting notification settings for: {}", connectId);
        NotificationSettings settings = settingsRepository.findNotificationSettings(connectId)
                .orElse(NotificationSettings.defaults(connectId));
        return new NotificationSettingsResponse(true, settings);
    }

    public NotificationSettingsResponse updateNotificationSettings(long connectId, UpdateNotificationSettingsRequest request) {
        log.info("Updating notification settings for: {}", connectId);

        NotificationSettings current = settingsRepository.findNotificationSettings(connectId)
                .orElse(NotificationSettings.defaults(connectId));

        NotificationSettings updated = NotificationSettings.builder()
                .connectId(connectId)
                .pushEnabled(request.pushEnabled() != null ? request.pushEnabled() : current.pushEnabled())
                .newMatchNotification(request.newMatchNotification() != null ? request.newMatchNotification() : current.newMatchNotification())
                .newMessageNotification(request.newMessageNotification() != null ? request.newMessageNotification() : current.newMessageNotification())
                .newLikeNotification(request.newLikeNotification() != null ? request.newLikeNotification() : current.newLikeNotification())
                .dailyBatchNotification(request.dailyBatchNotification() != null ? request.dailyBatchNotification() : current.dailyBatchNotification())
                .promotionalNotification(request.promotionalNotification() != null ? request.promotionalNotification() : current.promotionalNotification())
                .build();

        settingsRepository.saveNotificationSettings(updated);
        return new NotificationSettingsResponse(true, updated);
    }

    public EmailSettingsResponse getEmailSettings(long connectId) {
        log.info("Getting email settings for: {}", connectId);
        EmailSettings settings = settingsRepository.findEmailSettings(connectId)
                .orElse(EmailSettings.defaults(connectId));
        return new EmailSettingsResponse(true, settings);
    }

    public EmailSettingsResponse updateEmailSettings(long connectId, UpdateEmailSettingsRequest request) {
        log.info("Updating email settings for: {}", connectId);

        EmailSettings current = settingsRepository.findEmailSettings(connectId)
                .orElse(EmailSettings.defaults(connectId));

        EmailSettings updated = EmailSettings.builder()
                .connectId(connectId)
                .emailEnabled(request.emailEnabled() != null ? request.emailEnabled() : current.emailEnabled())
                .newMatchEmail(request.newMatchEmail() != null ? request.newMatchEmail() : current.newMatchEmail())
                .newMessageEmail(request.newMessageEmail() != null ? request.newMessageEmail() : current.newMessageEmail())
                .weeklyDigestEmail(request.weeklyDigestEmail() != null ? request.weeklyDigestEmail() : current.weeklyDigestEmail())
                .promotionalEmail(request.promotionalEmail() != null ? request.promotionalEmail() : current.promotionalEmail())
                .securityAlertEmail(request.securityAlertEmail() != null ? request.securityAlertEmail() : current.securityAlertEmail())
                .build();

        settingsRepository.saveEmailSettings(updated);
        return new EmailSettingsResponse(true, updated);
    }

    public PrivacySettingsResponse getPrivacySettings(long connectId) {
        log.info("Getting privacy settings for: {}", connectId);
        PrivacySettings settings = settingsRepository.findPrivacySettings(connectId)
                .orElse(PrivacySettings.defaults(connectId));
        return new PrivacySettingsResponse(true, settings);
    }

    public PrivacySettingsResponse updatePrivacySettings(long connectId, UpdatePrivacySettingsRequest request) {
        log.info("Updating privacy settings for: {}", connectId);

        PrivacySettings current = settingsRepository.findPrivacySettings(connectId)
                .orElse(PrivacySettings.defaults(connectId));

        PrivacySettings updated = PrivacySettings.builder()
                .connectId(connectId)
                .showOnlineStatus(request.showOnlineStatus() != null ? request.showOnlineStatus() : current.showOnlineStatus())
                .showLastActive(request.showLastActive() != null ? request.showLastActive() : current.showLastActive())
                .showReadReceipts(request.showReadReceipts() != null ? request.showReadReceipts() : current.showReadReceipts())
                .showProfileInSearch(request.showProfileInSearch() != null ? request.showProfileInSearch() : current.showProfileInSearch())
                .allowScreenshots(request.allowScreenshots() != null ? request.allowScreenshots() : current.allowScreenshots())
                .hideFromBlockedContacts(request.hideFromBlockedContacts() != null ? request.hideFromBlockedContacts() : current.hideFromBlockedContacts())
                .build();

        settingsRepository.savePrivacySettings(updated);
        return new PrivacySettingsResponse(true, updated);
    }

    public NotificationsListResponse getNotifications(long connectId, int limit) {
        log.info("Getting notifications for: {}", connectId);
        List<UserNotification> notifications = settingsRepository.findNotificationsByConnectId(connectId, limit);
        int unreadCount = settingsRepository.countUnreadNotifications(connectId);
        return new NotificationsListResponse(true, notifications, unreadCount);
    }

    public boolean markNotificationAsRead(String notificationId) {
        return settingsRepository.markNotificationAsRead(notificationId);
    }

    public boolean markAllNotificationsAsRead(long connectId) {
        return settingsRepository.markAllNotificationsAsRead(connectId);
    }

    public boolean clearAllNotifications(long connectId) {
        return settingsRepository.deleteAllNotifications(connectId);
    }
}

