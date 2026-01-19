package com.tpg.connect.profile.model.request;

import java.util.List;
import java.util.Map;

public record UpdateProfileRequest(
        String pronouns,
        String sexuality,
        String interestedIn,
        String jobTitle,
        String company,
        String university,
        String educationLevel,
        String religiousBeliefs,
        String hometown,
        String politics,
        String datingIntentions,
        String relationshipType,
        String height,
        String ethnicity,
        String children,
        String familyPlans,
        String pets,
        List<String> interests,
        List<Map<String, String>> writtenPrompts,
        Map<String, Boolean> fieldVisibility,
        String bio
) {}
