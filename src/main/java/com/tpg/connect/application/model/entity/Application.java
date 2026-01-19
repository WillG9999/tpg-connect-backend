package com.tpg.connect.application.model.entity;

import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
public record Application(
        String applicationId,
        String email,
        String hashedPassword,
        String firstName,
        String lastName,
        String dateOfBirth,
        String gender,
        String location,
        List<String> bestQualities,
        String reasonForJoining,
        List<String> photoUrls,
        String status,
        Instant createdAt,
        Instant reviewedAt,
        String reviewNotes,
        String rejectionReason
) {
}

