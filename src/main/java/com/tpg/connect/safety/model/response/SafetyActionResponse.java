package com.tpg.connect.safety.model.response;

public record SafetyActionResponse(
        boolean success,
        String actionId,
        String message
) {
}
