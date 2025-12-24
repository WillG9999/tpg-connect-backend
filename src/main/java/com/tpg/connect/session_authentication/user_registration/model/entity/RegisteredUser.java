package com.tpg.connect.session_authentication.user_registration.model.entity;

import lombok.Builder;

import java.time.Instant;

@Builder
public record RegisteredUser(
        long connectId,
        String email,
        String password,
        String firstName,
        String lastName,
        String dateOfBirth,
        String gender,
        String location,
        Instant createdAt
) {
}

