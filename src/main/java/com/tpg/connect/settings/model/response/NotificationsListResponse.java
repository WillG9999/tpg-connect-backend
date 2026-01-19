package com.tpg.connect.settings.model.response;

import com.tpg.connect.settings.model.entity.UserNotification;

import java.util.List;

public record NotificationsListResponse(
        boolean success,
        List<UserNotification> notifications,
        int unreadCount
) {
}

