package com.tpg.connect.safety.model.response;

import com.tpg.connect.safety.model.entity.Report;

import java.time.Instant;

public record ReportResponse(
        String reportId,
        long reporterConnectId,
        long reportedConnectId,
        String conversationId,
        String reason,
        String details,
        String status,
        Instant createdAt,
        Instant resolvedAt,
        String adminNotes
) {
    public static ReportResponse fromEntity(Report report) {
        return new ReportResponse(
                report.reportId(),
                report.reporterConnectId(),
                report.reportedConnectId(),
                report.conversationId(),
                report.reason().name(),
                report.details(),
                report.status().name(),
                report.createdAt(),
                report.resolvedAt(),
                report.adminNotes()
        );
    }
}
