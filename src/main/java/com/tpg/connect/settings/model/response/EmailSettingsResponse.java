package com.tpg.connect.settings.model.response;

import com.tpg.connect.settings.model.entity.EmailSettings;

public record EmailSettingsResponse(
        boolean success,
        EmailSettings settings
) {
}

