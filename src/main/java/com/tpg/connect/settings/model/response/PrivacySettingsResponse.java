package com.tpg.connect.settings.model.response;

import com.tpg.connect.settings.model.entity.PrivacySettings;

public record PrivacySettingsResponse(
        boolean success,
        PrivacySettings settings
) {
}

