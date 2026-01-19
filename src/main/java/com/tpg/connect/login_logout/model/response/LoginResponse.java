package com.tpg.connect.login_logout.model.response;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        String role
) {
}
