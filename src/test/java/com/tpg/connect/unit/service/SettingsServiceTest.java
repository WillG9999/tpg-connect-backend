package com.tpg.connect.unit.service;

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
import com.tpg.connect.settings.service.SettingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettingsServiceTest {

    @Mock
    private SettingsRepository settingsRepository;

    private SettingsService settingsService;

    @BeforeEach
    void setUp() {
        settingsService = new SettingsService(settingsRepository);
    }

    @Test
    void getNotificationSettings_returnsExistingSettings() {
        NotificationSettings existing = NotificationSettings.builder()
                .connectId(12345L)
                .pushEnabled(true)
                .newMatchNotification(false)
                .newMessageNotification(true)
                .newLikeNotification(true)
                .dailyBatchNotification(false)
                .promotionalNotification(false)
                .build();

        when(settingsRepository.findNotificationSettings(12345L)).thenReturn(Optional.of(existing));

        NotificationSettingsResponse response = settingsService.getNotificationSettings(12345L);

        assertTrue(response.success());
        assertFalse(response.settings().newMatchNotification());
        assertFalse(response.settings().dailyBatchNotification());
    }

    @Test
    void getNotificationSettings_returnsDefaultsWhenNoneExist() {
        when(settingsRepository.findNotificationSettings(12345L)).thenReturn(Optional.empty());

        NotificationSettingsResponse response = settingsService.getNotificationSettings(12345L);

        assertTrue(response.success());
        assertTrue(response.settings().pushEnabled());
        assertTrue(response.settings().newMatchNotification());
    }

    @Test
    void updateNotificationSettings_updatesOnlyProvidedFields() {
        NotificationSettings existing = NotificationSettings.defaults(12345L);
        UpdateNotificationSettingsRequest request = new UpdateNotificationSettingsRequest(
                null, false, null, null, null, true
        );

        when(settingsRepository.findNotificationSettings(12345L)).thenReturn(Optional.of(existing));
        when(settingsRepository.saveNotificationSettings(any())).thenReturn(true);

        NotificationSettingsResponse response = settingsService.updateNotificationSettings(12345L, request);

        assertTrue(response.success());
        assertFalse(response.settings().newMatchNotification());
        assertTrue(response.settings().promotionalNotification());
        assertTrue(response.settings().pushEnabled());
    }

    @Test
    void getEmailSettings_returnsDefaultsWhenNoneExist() {
        when(settingsRepository.findEmailSettings(12345L)).thenReturn(Optional.empty());

        EmailSettingsResponse response = settingsService.getEmailSettings(12345L);

        assertTrue(response.success());
        assertTrue(response.settings().emailEnabled());
        assertTrue(response.settings().securityAlertEmail());
    }

    @Test
    void updateEmailSettings_updatesSettings() {
        EmailSettings existing = EmailSettings.defaults(12345L);
        UpdateEmailSettingsRequest request = new UpdateEmailSettingsRequest(
                false, null, null, null, true, null
        );

        when(settingsRepository.findEmailSettings(12345L)).thenReturn(Optional.of(existing));
        when(settingsRepository.saveEmailSettings(any())).thenReturn(true);

        EmailSettingsResponse response = settingsService.updateEmailSettings(12345L, request);

        assertTrue(response.success());
        assertFalse(response.settings().emailEnabled());
        assertTrue(response.settings().promotionalEmail());
    }

    @Test
    void getPrivacySettings_returnsDefaultsWhenNoneExist() {
        when(settingsRepository.findPrivacySettings(12345L)).thenReturn(Optional.empty());

        PrivacySettingsResponse response = settingsService.getPrivacySettings(12345L);

        assertTrue(response.success());
        assertTrue(response.settings().showOnlineStatus());
        assertTrue(response.settings().showReadReceipts());
    }

    @Test
    void updatePrivacySettings_updatesSettings() {
        PrivacySettings existing = PrivacySettings.defaults(12345L);
        UpdatePrivacySettingsRequest request = new UpdatePrivacySettingsRequest(
                false, false, null, null, null, null
        );

        when(settingsRepository.findPrivacySettings(12345L)).thenReturn(Optional.of(existing));
        when(settingsRepository.savePrivacySettings(any())).thenReturn(true);

        PrivacySettingsResponse response = settingsService.updatePrivacySettings(12345L, request);

        assertTrue(response.success());
        assertFalse(response.settings().showOnlineStatus());
        assertFalse(response.settings().showLastActive());
    }

    @Test
    void getNotifications_returnsNotificationsList() {
        UserNotification notification = UserNotification.builder()
                .notificationId("notif1")
                .connectId(12345L)
                .type(UserNotification.NotificationType.NEW_MATCH)
                .title("New Match!")
                .message("You have a new match")
                .createdAt(Instant.now())
                .read(false)
                .build();

        when(settingsRepository.findNotificationsByConnectId(12345L, 50)).thenReturn(List.of(notification));
        when(settingsRepository.countUnreadNotifications(12345L)).thenReturn(1);

        NotificationsListResponse response = settingsService.getNotifications(12345L, 50);

        assertTrue(response.success());
        assertEquals(1, response.notifications().size());
        assertEquals(1, response.unreadCount());
    }

    @Test
    void markNotificationAsRead_callsRepository() {
        when(settingsRepository.markNotificationAsRead("notif1")).thenReturn(true);

        boolean result = settingsService.markNotificationAsRead("notif1");

        assertTrue(result);
        verify(settingsRepository).markNotificationAsRead("notif1");
    }

    @Test
    void markAllNotificationsAsRead_callsRepository() {
        when(settingsRepository.markAllNotificationsAsRead(12345L)).thenReturn(true);

        boolean result = settingsService.markAllNotificationsAsRead(12345L);

        assertTrue(result);
        verify(settingsRepository).markAllNotificationsAsRead(12345L);
    }

    @Test
    void clearAllNotifications_callsRepository() {
        when(settingsRepository.deleteAllNotifications(12345L)).thenReturn(true);

        boolean result = settingsService.clearAllNotifications(12345L);

        assertTrue(result);
        verify(settingsRepository).deleteAllNotifications(12345L);
    }
}

