package com.tpg.connect.settings.model.request;

public record UpdatePrivacySettingsRequest(
        Boolean showOnlineStatus,
        Boolean showLastActive,
        Boolean showReadReceipts,
        Boolean showProfileInSearch,
        Boolean allowScreenshots,
        Boolean hideFromBlockedContacts
) {
}

