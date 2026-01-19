package com.tpg.connect.matching.model.response;

import java.time.Instant;
import java.util.List;

public record ReceivedLikesResponse(
        boolean success,
        List<ReceivedLikeItem> likes,
        int totalCount
) {
    public record ReceivedLikeItem(
            String likeId,
            long likerConnectId,
            String firstName,
            int age,
            String location,
            String primaryPhotoUrl,
            String jobTitle,
            String bio,
            double compatibilityScore,
            Instant likedAt
    ) {
    }
}

