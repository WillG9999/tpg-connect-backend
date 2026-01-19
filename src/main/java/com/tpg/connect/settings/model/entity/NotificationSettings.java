package com.tpg.connect.settings.model.entity;

import lombok.Builder;

@Builder
public record NotificationSettings(
        long connectId,
        boolean pushEnabled,
        boolean newMatchNotification,
        boolean newMessageNotification,
        boolean newLikeNotification,
        boolean dailyBatchNotification,
        boolean promotionalNotification
) {
    public static NotificationSettings defaults(long connectId) {
        return NotificationSettings.builder()
                .connectId(connectId)
                .pushEnabled(true)
                .newMatchNotification(true)
                .newMessageNotification(true)
                .newLikeNotification(true)
                .dailyBatchNotification(true)
                .promotionalNotification(false)
                .build();
    }
}

