package com.tpg.connect.profile.model.request;

public record UpdatePreferencesRequest(
        String preferredGender,
        Integer minAge,
        Integer maxAge,
        Integer maxDistance,
        String distanceUnit,
        String datingIntention,
        String drinkingPreference,
        String smokingPreference,
        String religionPreference,
        String politicsPreference,
        Boolean showVerifiedOnly
) {
}

