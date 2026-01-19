package com.tpg.connect.password_reset.model.response;

public record PasswordResetResponse(
        boolean success,
        String message
) {
}

