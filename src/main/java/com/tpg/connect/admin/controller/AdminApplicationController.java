package com.tpg.connect.admin.controller;

import com.tpg.connect.admin.controller.api.AdminApplicationApi;
import com.tpg.connect.admin.model.request.ApproveApplicationRequest;
import com.tpg.connect.admin.model.request.RejectApplicationRequest;
import com.tpg.connect.admin.model.response.AdminUserDetailResponse;
import com.tpg.connect.admin.model.response.AdminUsersListResponse;
import com.tpg.connect.admin.model.response.ApplicationDetailResponse;
import com.tpg.connect.admin.model.response.ApplicationsPageResponse;
import com.tpg.connect.admin.model.response.DemographicsStatsResponse;
import com.tpg.connect.admin.service.AdminApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AdminApplicationController implements AdminApplicationApi {

    private final AdminApplicationService adminApplicationService;

    @Override
    public ResponseEntity<ApplicationsPageResponse> getAllApplications(int page, int size) {
        log.info("Admin request: Get all applications - page: {}, size: {}", page, size);
        ApplicationsPageResponse response = adminApplicationService.getAllApplications(page, size);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ApplicationsPageResponse> getPendingApplications(int page, int size) {
        log.info("Admin request: Get pending applications - page: {}, size: {}", page, size);
        ApplicationsPageResponse response = adminApplicationService.getPendingApplications(page, size);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ApplicationsPageResponse> getApplicationsByStatus(String status, int page, int size) {
        log.info("Admin request: Get applications by status {} - page: {}, size: {}", status, page, size);
        ApplicationsPageResponse response = adminApplicationService.getApplicationsByStatus(status, page, size);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ApplicationDetailResponse> getApplication(String applicationId) {
        log.info("Admin request: Get application {}", applicationId);
        return adminApplicationService.getApplication(applicationId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<ApplicationDetailResponse> approveApplication(
            String applicationId,
            ApproveApplicationRequest request
    ) {
        log.info("Admin request: Approve application {}", applicationId);
        return adminApplicationService.approveApplication(applicationId, request.notes())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<ApplicationDetailResponse> rejectApplication(
            String applicationId,
            RejectApplicationRequest request
    ) {
        log.info("Admin request: Reject application {}", applicationId);
        return adminApplicationService.rejectApplication(applicationId, request.rejectionReason(), request.notes())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<AdminUsersListResponse> getUsers(int page, int size, String search, String status, String sortBy, String sortDirection) {
        log.info("Admin request: Get users - page: {}, size: {}, search: {}, status: {}", page, size, search, status);
        AdminUsersListResponse response = adminApplicationService.getUsers(page, size, search, status);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<AdminUserDetailResponse> getUserDetail(long connectId) {
        log.info("Admin request: Get user detail for connectId: {}", connectId);
        return adminApplicationService.getUserDetail(connectId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<DemographicsStatsResponse> getDemographicsStats() {
        log.info("Admin request: Get demographics stats");
        DemographicsStatsResponse response = adminApplicationService.getDemographicsStats();
        return ResponseEntity.ok(response);
    }
}
