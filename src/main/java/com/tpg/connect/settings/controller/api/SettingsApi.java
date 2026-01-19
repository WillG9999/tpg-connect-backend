package com.tpg.connect.settings.controller.api;

import com.tpg.connect.settings.model.request.UpdateEmailSettingsRequest;
import com.tpg.connect.settings.model.request.UpdateNotificationSettingsRequest;
import com.tpg.connect.settings.model.request.UpdatePrivacySettingsRequest;
import com.tpg.connect.settings.model.response.EmailSettingsResponse;
import com.tpg.connect.settings.model.response.NotificationSettingsResponse;
import com.tpg.connect.settings.model.response.NotificationsListResponse;
import com.tpg.connect.settings.model.response.PrivacySettingsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Settings", description = "User settings management")
public interface SettingsApi {

    @Operation(summary = "Get push notification settings")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Settings retrieved")})
    @GetMapping("/v1/settings/notifications/push")
    ResponseEntity<NotificationSettingsResponse> getNotificationSettings();

    @Operation(summary = "Update push notification settings")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Settings updated")})
    @PutMapping("/v1/settings/notifications/push")
    ResponseEntity<NotificationSettingsResponse> updateNotificationSettings(@RequestBody UpdateNotificationSettingsRequest request);

    @Operation(summary = "Get email notification settings")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Settings retrieved")})
    @GetMapping("/v1/settings/notifications/email")
    ResponseEntity<EmailSettingsResponse> getEmailSettings();

    @Operation(summary = "Update email notification settings")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Settings updated")})
    @PutMapping("/v1/settings/notifications/email")
    ResponseEntity<EmailSettingsResponse> updateEmailSettings(@RequestBody UpdateEmailSettingsRequest request);

    @Operation(summary = "Get privacy settings")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Settings retrieved")})
    @GetMapping("/v1/settings/privacy")
    ResponseEntity<PrivacySettingsResponse> getPrivacySettings();

    @Operation(summary = "Update privacy settings")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Settings updated")})
    @PutMapping("/v1/settings/privacy")
    ResponseEntity<PrivacySettingsResponse> updatePrivacySettings(@RequestBody UpdatePrivacySettingsRequest request);

    @Operation(summary = "Get notifications list")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Notifications retrieved")})
    @GetMapping("/v1/notifications")
    ResponseEntity<NotificationsListResponse> getNotifications(@RequestParam(defaultValue = "50") int limit);

    @Operation(summary = "Mark notification as read")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Notification marked as read")})
    @PostMapping("/v1/notifications/{notificationId}/read")
    ResponseEntity<Map<String, Object>> markNotificationAsRead(@PathVariable String notificationId);

    @Operation(summary = "Mark all notifications as read")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "All notifications marked as read")})
    @PostMapping("/v1/notifications/read-all")
    ResponseEntity<Map<String, Object>> markAllNotificationsAsRead();

    @Operation(summary = "Clear all notifications")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "All notifications cleared")})
    @DeleteMapping("/v1/notifications")
    ResponseEntity<Map<String, Object>> clearAllNotifications();
}

