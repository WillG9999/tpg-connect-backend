package com.tpg.connect.safety.controller.api;

import com.tpg.connect.safety.model.request.ResolveReportRequest;
import com.tpg.connect.safety.model.response.ReportResponse;
import com.tpg.connect.safety.model.response.SafetyActionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin Safety", description = "Admin endpoints for report management")
@RequestMapping("/v1/admin/reports")
public interface AdminSafetyApi {

    @Operation(summary = "Get pending reports", description = "Get all pending reports for review")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reports retrieved successfully")
    })
    @GetMapping("/pending")
    ResponseEntity<List<ReportResponse>> getPendingReports();

    @Operation(summary = "Get all reports", description = "Get all reports")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reports retrieved successfully")
    })
    @GetMapping
    ResponseEntity<List<ReportResponse>> getAllReports();

    @Operation(summary = "Resolve a report", description = "Resolve a report with a decision")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report resolved successfully"),
            @ApiResponse(responseCode = "404", description = "Report not found")
    })
    @PutMapping("/{reportId}/resolve")
    ResponseEntity<SafetyActionResponse> resolveReport(
            @PathVariable String reportId,
            @RequestBody ResolveReportRequest request
    );
}
