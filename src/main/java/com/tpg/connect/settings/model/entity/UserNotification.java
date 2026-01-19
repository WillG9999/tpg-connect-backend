package com.tpg.connect.settings.model.entity;

import lombok.Builder;

import java.time.Instant;

@Builder
public record UserNotification(
        String notificationId,
        long connectId,
        NotificationType type,
        String title,
        String message,
        String actionUrl,
        Long relatedConnectId,
        Instant createdAt,
        boolean read
) {
    public enum NotificationType {
        NEW_MATCH,
        NEW_MESSAGE,
        NEW_LIKE,
        DAILY_BATCH_READY,
        PROFILE_VIEW,
        SYSTEM_ANNOUNCEMENT,
        PROMOTIONAL
    }
}

