package com.tpg.connect.profile.model.entity;

import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
public record UserProfile(
        long connectId,
        String email,
        String firstName,
        String lastName,
        String dateOfBirth,
        String gender,
        String location,
        List<String> interests,
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
        String zodiacSign,
        List<String> photoUrls,
        List<Map<String, String>> writtenPrompts,
        Map<String, Boolean> fieldVisibility,
        String bio
) {}
