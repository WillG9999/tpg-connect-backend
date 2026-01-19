package com.tpg.connect.matching.model.entity;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class DailySuggestion {
    private long suggestedConnectId;
    private double compatibilityScore;
    private int stabilityRank;
    private boolean viewed;
    private ProfileSummary profile;

    @Data
    @Builder
    public static class ProfileSummary {
        private String firstName;
        private int age;
        private String location;
        private String primaryPhotoUrl;
        private String jobTitle;
        private String bio;
        private List<String> interests;
    }
}

