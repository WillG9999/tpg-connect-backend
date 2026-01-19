package com.tpg.connect.settings.model.entity;

import lombok.Builder;

@Builder
public record PrivacySettings(
        long connectId,
        boolean showOnlineStatus,
        boolean showLastActive,
        boolean showReadReceipts,
        boolean showProfileInSearch,
        boolean allowScreenshots,
        boolean hideFromBlockedContacts
) {
    public static PrivacySettings defaults(long connectId) {
        return PrivacySettings.builder()
                .connectId(connectId)
                .showOnlineStatus(true)
                .showLastActive(true)
                .showReadReceipts(true)
                .showProfileInSearch(true)
                .allowScreenshots(true)
                .hideFromBlockedContacts(true)
                .build();
    }
}

