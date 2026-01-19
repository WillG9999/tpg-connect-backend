package com.tpg.connect.settings.model.request;

public record UpdateNotificationSettingsRequest(
        Boolean pushEnabled,
        Boolean newMatchNotification,
        Boolean newMessageNotification,
        Boolean newLikeNotification,
        Boolean dailyBatchNotification,
        Boolean promotionalNotification
) {
}

