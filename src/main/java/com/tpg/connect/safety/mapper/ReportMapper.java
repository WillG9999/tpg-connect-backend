package com.tpg.connect.safety.mapper;

import com.tpg.connect.safety.model.entity.Report;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
public class ReportMapper {

    public Map<String, Object> toDocument(Report report) {
        Map<String, Object> doc = new HashMap<>();
        doc.put("reporterConnectId", report.reporterConnectId());
        doc.put("reportedConnectId", report.reportedConnectId());
        doc.put("conversationId", report.conversationId());
        doc.put("reason", report.reason().name());
        doc.put("details", report.details());
        doc.put("status", report.status().name());
        doc.put("createdAt", report.createdAt().toEpochMilli());
        doc.put("resolvedAt", report.resolvedAt() != null ? report.resolvedAt().toEpochMilli() : null);
        doc.put("adminNotes", report.adminNotes());
        return doc;
    }

    public Report fromDocument(Map<String, Object> data, String reportId) {
        return Report.builder()
                .reportId(reportId)
                .reporterConnectId(((Number) data.get("reporterConnectId")).longValue())
                .reportedConnectId(((Number) data.get("reportedConnectId")).longValue())
                .conversationId((String) data.get("conversationId"))
                .reason(Report.ReportReason.valueOf((String) data.get("reason")))
                .details((String) data.get("details"))
                .status(Report.ReportStatus.valueOf((String) data.get("status")))
                .createdAt(Instant.ofEpochMilli(((Number) data.get("createdAt")).longValue()))
                .resolvedAt(data.get("resolvedAt") != null ? Instant.ofEpochMilli(((Number) data.get("resolvedAt")).longValue()) : null)
                .adminNotes((String) data.get("adminNotes"))
                .build();
    }
}
