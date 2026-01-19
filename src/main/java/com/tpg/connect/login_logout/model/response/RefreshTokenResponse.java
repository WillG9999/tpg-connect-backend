package com.tpg.connect.login_logout.model.response;

public record RefreshTokenResponse(
        String accessToken,
        String refreshToken,
        long expiresIn
) {
}

