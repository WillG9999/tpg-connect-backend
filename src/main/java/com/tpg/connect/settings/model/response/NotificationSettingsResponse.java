package com.tpg.connect.settings.model.response;

import com.tpg.connect.settings.model.entity.NotificationSettings;

public record NotificationSettingsResponse(
        boolean success,
        NotificationSettings settings
) {
}

