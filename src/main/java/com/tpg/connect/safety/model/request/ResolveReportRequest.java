package com.tpg.connect.safety.model.request;

import com.tpg.connect.safety.model.entity.Report.ReportStatus;

public record ResolveReportRequest(
        ReportStatus status,
        String adminNotes
) {
}
