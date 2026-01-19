package com.tpg.connect.profile.model.response;

import java.util.List;
import java.util.Map;

public record ProfileResponse(
        String name,
        int age,
        String location,
        List<String> interests,
        String pronouns,
        String gender,
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
        String zodiacSign,
        List<String> photos,
        List<Map<String, String>> writtenPrompts,
        Map<String, Boolean> fieldVisibility,
        String bio
) {}
