package com.tpg.connect.profile.model.entity;

import lombok.Builder;

@Builder
public record UserPreferences(
        long connectId,
        String preferredGender,
        int minAge,
        int maxAge,
        int maxDistance,
        String distanceUnit,
        String datingIntention,
        String drinkingPreference,
        String smokingPreference,
        String religionPreference,
        String politicsPreference,
        Boolean showVerifiedOnly
) {
    public static UserPreferences defaultPreferences(long connectId) {
        return UserPreferences.builder()
                .connectId(connectId)
                .preferredGender("Everyone")
                .minAge(18)
                .maxAge(50)
                .maxDistance(50)
                .distanceUnit("miles")
                .showVerifiedOnly(false)
                .build();
    }
}

