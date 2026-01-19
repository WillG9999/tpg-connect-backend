package com.tpg.connect.settings.controller;

import com.tpg.connect.common.services.authentication.JsonWebTokenValidatorService;
import com.tpg.connect.settings.controller.api.SettingsApi;
import com.tpg.connect.settings.model.request.UpdateEmailSettingsRequest;
import com.tpg.connect.settings.model.request.UpdateNotificationSettingsRequest;
import com.tpg.connect.settings.model.request.UpdatePrivacySettingsRequest;
import com.tpg.connect.settings.model.response.EmailSettingsResponse;
import com.tpg.connect.settings.model.response.NotificationSettingsResponse;
import com.tpg.connect.settings.model.response.NotificationsListResponse;
import com.tpg.connect.settings.model.response.PrivacySettingsResponse;
import com.tpg.connect.settings.service.SettingsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.tpg.connect.common.constants.HeaderConstants.X_AUTHORISATION;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SettingsController implements SettingsApi {

    private final SettingsService settingsService;
    private final JsonWebTokenValidatorService jwtValidatorService;
    private final HttpServletRequest httpServletRequest;

    @Override
    public ResponseEntity<NotificationSettingsResponse> getNotificationSettings() {
        long connectId = extractConnectId();
        log.info("Getting notification settings for: {}", connectId);
        return ResponseEntity.ok(settingsService.getNotificationSettings(connectId));
    }

    @Override
    public ResponseEntity<NotificationSettingsResponse> updateNotificationSettings(UpdateNotificationSettingsRequest request) {
        long connectId = extractConnectId();
        log.info("Updating notification settings for: {}", connectId);
        return ResponseEntity.ok(settingsService.updateNotificationSettings(connectId, request));
    }

    @Override
    public ResponseEntity<EmailSettingsResponse> getEmailSettings() {
        long connectId = extractConnectId();
        log.info("Getting email settings for: {}", connectId);
        return ResponseEntity.ok(settingsService.getEmailSettings(connectId));
    }

    @Override
    public ResponseEntity<EmailSettingsResponse> updateEmailSettings(UpdateEmailSettingsRequest request) {
        long connectId = extractConnectId();
        log.info("Updating email settings for: {}", connectId);
        return ResponseEntity.ok(settingsService.updateEmailSettings(connectId, request));
    }

    @Override
    public ResponseEntity<PrivacySettingsResponse> getPrivacySettings() {
        long connectId = extractConnectId();
        log.info("Getting privacy settings for: {}", connectId);
        return ResponseEntity.ok(settingsService.getPrivacySettings(connectId));
    }

    @Override
    public ResponseEntity<PrivacySettingsResponse> updatePrivacySettings(UpdatePrivacySettingsRequest request) {
        long connectId = extractConnectId();
        log.info("Updating privacy settings for: {}", connectId);
        return ResponseEntity.ok(settingsService.updatePrivacySettings(connectId, request));
    }

    @Override
    public ResponseEntity<NotificationsListResponse> getNotifications(int limit) {
        long connectId = extractConnectId();
        log.info("Getting notifications for: {}", connectId);
        return ResponseEntity.ok(settingsService.getNotifications(connectId, limit));
    }

    @Override
    public ResponseEntity<Map<String, Object>> markNotificationAsRead(String notificationId) {
        log.info("Marking notification as read: {}", notificationId);
        boolean success = settingsService.markNotificationAsRead(notificationId);
        return ResponseEntity.ok(Map.of("success", success));
    }

    @Override
    public ResponseEntity<Map<String, Object>> markAllNotificationsAsRead() {
        long connectId = extractConnectId();
        log.info("Marking all notifications as read for: {}", connectId);
        boolean success = settingsService.markAllNotificationsAsRead(connectId);
        return ResponseEntity.ok(Map.of("success", success));
    }

    @Override
    public ResponseEntity<Map<String, Object>> clearAllNotifications() {
        long connectId = extractConnectId();
        log.info("Clearing all notifications for: {}", connectId);
        boolean success = settingsService.clearAllNotifications(connectId);
        return ResponseEntity.ok(Map.of("success", success));
    }

    private long extractConnectId() {
        String authHeader = httpServletRequest.getHeader(X_AUTHORISATION);
        String token = authHeader.replace("Bearer ", "");
        return jwtValidatorService.extractConnectId(token);
    }
}

