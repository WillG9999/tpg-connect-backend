package com.tpg.connect.safety.model.request;

import com.tpg.connect.safety.model.entity.Report.ReportReason;

public record ReportUserRequest(
        ReportReason reason,
        String details,
        String conversationId
) {
}
