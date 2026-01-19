package com.tpg.connect.password_reset.model.response;

public record VerifyResetTokenResponse(
        boolean valid,
        String email
) {
}

