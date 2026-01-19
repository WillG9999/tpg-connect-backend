package com.tpg.connect.matching.model.response;

public record LikeActionResponse(
        boolean success,
        String message,
        NewMatchInfo newMatch
) {
    public record NewMatchInfo(
            String matchId,
            long matchedConnectId,
            String matchedName,
            String conversationId
    ) {
    }

    public static LikeActionResponse success(String message) {
        return new LikeActionResponse(true, message, null);
    }

    public static LikeActionResponse successWithMatch(String message, NewMatchInfo matchInfo) {
        return new LikeActionResponse(true, message, matchInfo);
    }
}

