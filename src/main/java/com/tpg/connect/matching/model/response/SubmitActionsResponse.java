package com.tpg.connect.matching.model.response;

import java.util.List;

public record SubmitActionsResponse(
        boolean success,
        int processedCount,
        List<NewMatchNotification> newMatches
) {
    public record NewMatchNotification(
            String matchId,
            long matchedConnectId,
            String matchedName,
            String conversationId
    ) {
    }
}

