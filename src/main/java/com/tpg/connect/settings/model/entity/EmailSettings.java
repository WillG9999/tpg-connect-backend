package com.tpg.connect.settings.model.entity;

import lombok.Builder;

@Builder
public record EmailSettings(
        long connectId,
        boolean emailEnabled,
        boolean newMatchEmail,
        boolean newMessageEmail,
        boolean weeklyDigestEmail,
        boolean promotionalEmail,
        boolean securityAlertEmail
) {
    public static EmailSettings defaults(long connectId) {
        return EmailSettings.builder()
                .connectId(connectId)
                .emailEnabled(true)
                .newMatchEmail(true)
                .newMessageEmail(false)
                .weeklyDigestEmail(true)
                .promotionalEmail(false)
                .securityAlertEmail(true)
                .build();
    }
}

