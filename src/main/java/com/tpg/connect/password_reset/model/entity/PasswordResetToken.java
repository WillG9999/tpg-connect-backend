package com.tpg.connect.password_reset.model.entity;

import lombok.Builder;

import java.time.Instant;

@Builder
public record PasswordResetToken(
        String token,
        String email,
        Instant expiresAt,
        boolean used
) {
}

