package com.tpg.connect.application.model.response;

public record ApplicationStatusResponse(
        String applicationId,
        String status,
        String submittedAt
) {
}

