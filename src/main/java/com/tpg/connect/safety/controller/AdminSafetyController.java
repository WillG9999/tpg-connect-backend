package com.tpg.connect.safety.controller;

import com.tpg.connect.safety.controller.api.AdminSafetyApi;
import com.tpg.connect.safety.model.request.ResolveReportRequest;
import com.tpg.connect.safety.model.response.ReportResponse;
import com.tpg.connect.safety.model.response.SafetyActionResponse;
import com.tpg.connect.safety.service.SafetyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AdminSafetyController implements AdminSafetyApi {

    private final SafetyService safetyService;

    @Override
    public ResponseEntity<List<ReportResponse>> getPendingReports() {
        log.info("Getting pending reports");
        List<ReportResponse> reports = safetyService.getPendingReports().stream()
                .map(ReportResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(reports);
    }

    @Override
    public ResponseEntity<List<ReportResponse>> getAllReports() {
        log.info("Getting all reports");
        List<ReportResponse> reports = safetyService.getAllReports().stream()
                .map(ReportResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(reports);
    }

    @Override
    public ResponseEntity<SafetyActionResponse> resolveReport(String reportId, ResolveReportRequest request) {
        log.info("Resolving report: {} with status: {}", reportId, request.status());
        boolean success = safetyService.resolveReport(reportId, request.status(), request.adminNotes());

        if (success) {
            return ResponseEntity.ok(new SafetyActionResponse(true, reportId, "Report resolved successfully"));
        }
        return ResponseEntity.ok(new SafetyActionResponse(false, reportId, "Failed to resolve report"));
    }
}
