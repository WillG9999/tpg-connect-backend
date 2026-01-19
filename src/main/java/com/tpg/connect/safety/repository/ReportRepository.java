package com.tpg.connect.safety.repository;

import com.google.cloud.firestore.Firestore;
import com.tpg.connect.safety.mapper.ReportMapper;
import com.tpg.connect.safety.model.entity.Report;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ReportRepository {

    private static final String COLLECTION_NAME = "Reports";
    private final Firestore firestore;
    private final ReportMapper reportMapper;

    public Report save(Report report) {
        try {
            String reportId = report.reportId() != null ? report.reportId() : UUID.randomUUID().toString();
            Report reportToSave = Report.builder()
                    .reportId(reportId)
                    .reporterConnectId(report.reporterConnectId())
                    .reportedConnectId(report.reportedConnectId())
                    .conversationId(report.conversationId())
                    .reason(report.reason())
                    .details(report.details())
                    .status(report.status())
                    .createdAt(report.createdAt())
                    .resolvedAt(report.resolvedAt())
                    .adminNotes(report.adminNotes())
                    .build();

            Map<String, Object> data = reportMapper.toDocument(reportToSave);
            firestore.collection(COLLECTION_NAME).document(reportId).set(data).get();
            log.info("Report saved: {}", reportId);
            return reportToSave;
        } catch (Exception e) {
            log.error("Failed to save report", e);
            throw new RuntimeException("Failed to save report", e);
        }
    }

    public List<Report> findPendingReports() {
        try {
            var docs = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("status", "PENDING")
                    .get().get().getDocuments();
            return docs.stream()
                    .map(doc -> reportMapper.fromDocument(doc.getData(), doc.getId()))
                    .toList();
        } catch (Exception e) {
            log.error("Failed to find pending reports", e);
            return List.of();
        }
    }

    public List<Report> findAllReports() {
        try {
            var docs = firestore.collection(COLLECTION_NAME)
                    .get().get().getDocuments();
            return docs.stream()
                    .map(doc -> reportMapper.fromDocument(doc.getData(), doc.getId()))
                    .toList();
        } catch (Exception e) {
            log.error("Failed to find all reports", e);
            return List.of();
        }
    }

    public Optional<Report> findById(String reportId) {
        try {
            var doc = firestore.collection(COLLECTION_NAME).document(reportId).get().get();
            if (doc.exists()) {
                return Optional.of(reportMapper.fromDocument(doc.getData(), doc.getId()));
            }
            return Optional.empty();
        } catch (Exception e) {
            log.error("Failed to find report: {}", reportId, e);
            return Optional.empty();
        }
    }

    public boolean updateStatus(String reportId, Report.ReportStatus status, String adminNotes) {
        try {
            firestore.collection(COLLECTION_NAME).document(reportId)
                    .update("status", status.name(), "adminNotes", adminNotes, "resolvedAt", System.currentTimeMillis());
            return true;
        } catch (Exception e) {
            log.error("Failed to update report status: {}", reportId, e);
            return false;
        }
    }
}
