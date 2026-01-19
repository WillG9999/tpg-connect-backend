package com.tpg.connect.settings.model.request;

public record UpdateEmailSettingsRequest(
        Boolean emailEnabled,
        Boolean newMatchEmail,
        Boolean newMessageEmail,
        Boolean weeklyDigestEmail,
        Boolean promotionalEmail,
        Boolean securityAlertEmail
) {
}

