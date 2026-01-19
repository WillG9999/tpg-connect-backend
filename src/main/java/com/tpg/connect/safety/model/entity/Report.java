package com.tpg.connect.safety.model.entity;

import lombok.Builder;

import java.time.Instant;

@Builder
public record Report(
        String reportId,
        long reporterConnectId,
        long reportedConnectId,
        String conversationId,
        ReportReason reason,
        String details,
        ReportStatus status,
        Instant createdAt,
        Instant resolvedAt,
        String adminNotes
) {
    public enum ReportStatus {
        PENDING,
        REVIEWED,
        RESOLVED,
        DISMISSED
    }

    public enum ReportReason {
        INAPPROPRIATE_CONTENT,
        HARASSMENT,
        SPAM,
        FAKE_PROFILE,
        OTHER
    }
}
