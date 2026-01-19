package com.tpg.connect.admin.model.response;

import java.util.List;

public record ApplicationDetailResponse(
        String applicationId,
        String email,
        String firstName,
        String lastName,
        String dateOfBirth,
        String gender,
        String location,
        List<String> bestQualities,
        String reasonForJoining,
        List<String> photoUrls,
        String status,
        String submittedAt,
        String reviewedAt,
        String reviewNotes,
        String rejectionReason
) {}

