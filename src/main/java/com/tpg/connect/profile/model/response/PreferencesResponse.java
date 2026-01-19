package com.tpg.connect.profile.model.response;

import com.tpg.connect.profile.model.entity.UserPreferences;

public record PreferencesResponse(
        boolean success,
        UserPreferences preferences
) {
    public static PreferencesResponse success(UserPreferences preferences) {
        return new PreferencesResponse(true, preferences);
    }
}

