package com.tpg.connect.settings.model.response;

import com.tpg.connect.profile.model.entity.UserProfile;
import com.tpg.connect.settings.model.entity.EmailSettings;
import com.tpg.connect.settings.model.entity.NotificationSettings;
import com.tpg.connect.settings.model.entity.PrivacySettings;

import java.time.Instant;

public record AccountDataResponse(
        boolean success,
        UserProfile profile,
        NotificationSettings notificationSettings,
        EmailSettings emailSettings,
        PrivacySettings privacySettings,
        Instant exportedAt
) {
}

