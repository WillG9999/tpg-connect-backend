package com.tpg.connect.admin.model.response;

public record AdminUserSummaryResponse(
        String connectId,
        String displayName,
        String email,
        String location,
        String profilePhotoUrl,
        String statusDisplayText,
        boolean hasActiveReports,
        Integer totalMatches,
        Integer totalConversations
) {}
