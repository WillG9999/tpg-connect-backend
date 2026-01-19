package com.tpg.connect.admin.controller.api;

import com.tpg.connect.admin.model.request.ApproveApplicationRequest;
import com.tpg.connect.admin.model.request.RejectApplicationRequest;
import com.tpg.connect.admin.model.response.ApplicationDetailResponse;
import com.tpg.connect.admin.model.response.ApplicationsPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.tpg.connect.common.constants.ConnectApiEndpointConstants.*;

@Tag(name = "Admin - Applications", description = "Admin endpoints for managing user applications")
@SecurityRequirement(name = "bearerAuth")
public interface AdminApplicationApi {

    @Operation(summary = "Get all applications", description = "Retrieve all applications with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paginated list of all applications"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping(ADMIN_APPLICATIONS_ALL)
    ResponseEntity<ApplicationsPageResponse> getAllApplications(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size
    );

    @Operation(summary = "Get pending applications", description = "Retrieve pending applications with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paginated list of pending applications"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping(ADMIN_APPLICATIONS_PENDING)
    ResponseEntity<ApplicationsPageResponse> getPendingApplications(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size
    );

    @Operation(summary = "Get applications by status", description = "Retrieve applications by status with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paginated list of applications"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping(ADMIN_APPLICATIONS_BY_STATUS)
    ResponseEntity<ApplicationsPageResponse> getApplicationsByStatus(
            @Parameter(description = "Application status (pending, approved, rejected)", required = true)
            @PathVariable String status,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size
    );

    @Operation(summary = "Get application by ID", description = "Retrieve detailed information for a specific application")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application details"),
            @ApiResponse(responseCode = "404", description = "Application not found"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping(ADMIN_APPLICATIONS_BY_ID)
    ResponseEntity<ApplicationDetailResponse> getApplication(
            @Parameter(description = "Application ID", required = true, example = "APP-1234567890")
            @PathVariable String applicationId
    );

    @Operation(summary = "Approve application", description = "Approve an application and create user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application approved successfully"),
            @ApiResponse(responseCode = "404", description = "Application not found"),
            @ApiResponse(responseCode = "400", description = "Application already processed"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @PutMapping(ADMIN_APPLICATIONS_APPROVE)
    ResponseEntity<ApplicationDetailResponse> approveApplication(
            @Parameter(description = "Application ID", required = true)
            @PathVariable String applicationId,
            @Valid @RequestBody ApproveApplicationRequest request
    );

    @Operation(summary = "Reject application", description = "Reject an application with reason")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application rejected successfully"),
            @ApiResponse(responseCode = "404", description = "Application not found"),
            @ApiResponse(responseCode = "400", description = "Application already processed"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @PutMapping(ADMIN_APPLICATIONS_REJECT)
    ResponseEntity<ApplicationDetailResponse> rejectApplication(
            @Parameter(description = "Application ID", required = true)
            @PathVariable String applicationId,
            @Valid @RequestBody RejectApplicationRequest request
    );
}

