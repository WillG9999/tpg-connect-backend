package com.tpg.connect.profile.mapper;

import com.tpg.connect.profile.model.entity.UserPreferences;
import com.tpg.connect.profile.model.request.UpdatePreferencesRequest;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PreferencesMapper {

    public Map<String, Object> toDocument(UserPreferences preferences) {
        Map<String, Object> data = new HashMap<>();
        data.put("connectId", preferences.connectId());
        data.put("preferredGender", preferences.preferredGender());
        data.put("minAge", preferences.minAge());
        data.put("maxAge", preferences.maxAge());
        data.put("maxDistance", preferences.maxDistance());
        data.put("distanceUnit", preferences.distanceUnit());
        data.put("datingIntention", preferences.datingIntention());
        data.put("drinkingPreference", preferences.drinkingPreference());
        data.put("smokingPreference", preferences.smokingPreference());
        data.put("religionPreference", preferences.religionPreference());
        data.put("politicsPreference", preferences.politicsPreference());
        data.put("showVerifiedOnly", preferences.showVerifiedOnly());
        return data;
    }

    public UserPreferences toEntity(Map<String, Object> data, long connectId) {
        return UserPreferences.builder()
                .connectId(connectId)
                .preferredGender(getString(data, "preferredGender"))
                .minAge(getInt(data, "minAge", 18))
                .maxAge(getInt(data, "maxAge", 50))
                .maxDistance(getInt(data, "maxDistance", 50))
                .distanceUnit(getString(data, "distanceUnit", "miles"))
                .datingIntention(getString(data, "datingIntention"))
                .drinkingPreference(getString(data, "drinkingPreference"))
                .smokingPreference(getString(data, "smokingPreference"))
                .religionPreference(getString(data, "religionPreference"))
                .politicsPreference(getString(data, "politicsPreference"))
                .showVerifiedOnly(getBoolean(data, "showVerifiedOnly", false))
                .build();
    }

    public UserPreferences applyUpdate(UserPreferences current, UpdatePreferencesRequest request) {
        return UserPreferences.builder()
                .connectId(current.connectId())
                .preferredGender(request.preferredGender() != null ? request.preferredGender() : current.preferredGender())
                .minAge(request.minAge() != null ? request.minAge() : current.minAge())
                .maxAge(request.maxAge() != null ? request.maxAge() : current.maxAge())
                .maxDistance(request.maxDistance() != null ? request.maxDistance() : current.maxDistance())
                .distanceUnit(request.distanceUnit() != null ? request.distanceUnit() : current.distanceUnit())
                .datingIntention(request.datingIntention() != null ? request.datingIntention() : current.datingIntention())
                .drinkingPreference(request.drinkingPreference() != null ? request.drinkingPreference() : current.drinkingPreference())
                .smokingPreference(request.smokingPreference() != null ? request.smokingPreference() : current.smokingPreference())
                .religionPreference(request.religionPreference() != null ? request.religionPreference() : current.religionPreference())
                .politicsPreference(request.politicsPreference() != null ? request.politicsPreference() : current.politicsPreference())
                .showVerifiedOnly(request.showVerifiedOnly() != null ? request.showVerifiedOnly() : current.showVerifiedOnly())
                .build();
    }

    private String getString(Map<String, Object> data, String key) {
        return getString(data, key, null);
    }

    private String getString(Map<String, Object> data, String key, String defaultValue) {
        Object value = data.get(key);
        return value != null ? (String) value : defaultValue;
    }

    private int getInt(Map<String, Object> data, String key, int defaultValue) {
        Object value = data.get(key);
        return value != null ? ((Number) value).intValue() : defaultValue;
    }

    private boolean getBoolean(Map<String, Object> data, String key, boolean defaultValue) {
        Object value = data.get(key);
        return value != null ? (Boolean) value : defaultValue;
    }
}

