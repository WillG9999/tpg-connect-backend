package com.tpg.connect.unit.controller;

import com.tpg.connect.admin.controller.AdminApplicationController;
import com.tpg.connect.admin.model.request.ApproveApplicationRequest;
import com.tpg.connect.admin.model.request.RejectApplicationRequest;
import com.tpg.connect.admin.model.response.ApplicationDetailResponse;
import com.tpg.connect.admin.model.response.ApplicationsPageResponse;
import com.tpg.connect.admin.service.AdminApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminApplicationControllerTest {

    @Mock
    private AdminApplicationService adminApplicationService;

    private AdminApplicationController controller;

    private ApplicationDetailResponse testApplicationResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new AdminApplicationController(adminApplicationService);

        testApplicationResponse = new ApplicationDetailResponse(
                "APP-123456",
                "test@example.com",
                "John",
                "Doe",
                "1990-01-01",
                "Male",
                "San Francisco",
                List.of("Honest", "Kind"),
                "Looking for connection",
                List.of("photo1.jpg"),
                "pending",
                "2026-01-15T10:00:00Z",
                null,
                null,
                null
        );
    }

    @Test
    void getAllApplications_returnsOkWithPaginatedApplications() {
        ApplicationsPageResponse response = new ApplicationsPageResponse(
                List.of(testApplicationResponse),
                0,
                10,
                1,
                1,
                true,
                true
        );
        when(adminApplicationService.getAllApplications(0, 10)).thenReturn(response);

        ResponseEntity<ApplicationsPageResponse> result = controller.getAllApplications(0, 10);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().totalElements());
        assertEquals(0, result.getBody().page());
    }

    @Test
    void getPendingApplications_returnsOkWithPaginatedApplications() {
        ApplicationsPageResponse response = new ApplicationsPageResponse(
                List.of(testApplicationResponse),
                0,
                10,
                1,
                1,
                true,
                true
        );
        when(adminApplicationService.getPendingApplications(0, 10)).thenReturn(response);

        ResponseEntity<ApplicationsPageResponse> result = controller.getPendingApplications(0, 10);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().totalElements());
    }

    @Test
    void getApplicationsByStatus_returnsOkWithPaginatedApplications() {
        ApplicationsPageResponse response = new ApplicationsPageResponse(
                List.of(testApplicationResponse),
                0,
                10,
                1,
                1,
                true,
                true
        );
        when(adminApplicationService.getApplicationsByStatus("approved", 0, 10)).thenReturn(response);

        ResponseEntity<ApplicationsPageResponse> result = controller.getApplicationsByStatus("approved", 0, 10);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
    }

    @Test
    void getApplication_returnsOkWhenFound() {
        when(adminApplicationService.getApplication("APP-123456")).thenReturn(Optional.of(testApplicationResponse));

        ResponseEntity<ApplicationDetailResponse> result = controller.getApplication("APP-123456");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("APP-123456", result.getBody().applicationId());
    }

    @Test
    void getApplication_returnsNotFoundWhenNotExists() {
        when(adminApplicationService.getApplication("APP-NOTFOUND")).thenReturn(Optional.empty());

        ResponseEntity<ApplicationDetailResponse> result = controller.getApplication("APP-NOTFOUND");

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    void approveApplication_returnsOkWhenSuccessful() {
        ApplicationDetailResponse approvedResponse = new ApplicationDetailResponse(
                "APP-123456",
                "test@example.com",
                "John",
                "Doe",
                "1990-01-01",
                "Male",
                "San Francisco",
                List.of("Honest", "Kind"),
                "Looking for connection",
                List.of("photo1.jpg"),
                "approved",
                "2026-01-15T10:00:00Z",
                "2026-01-16T14:00:00Z",
                "Great profile",
                null
        );
        ApproveApplicationRequest request = new ApproveApplicationRequest("Great profile");
        when(adminApplicationService.approveApplication("APP-123456", "Great profile"))
                .thenReturn(Optional.of(approvedResponse));

        ResponseEntity<ApplicationDetailResponse> result = controller.approveApplication("APP-123456", request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("approved", result.getBody().status());
    }

    @Test
    void approveApplication_returnsNotFoundWhenApplicationNotExists() {
        ApproveApplicationRequest request = new ApproveApplicationRequest("Notes");
        when(adminApplicationService.approveApplication("APP-NOTFOUND", "Notes"))
                .thenReturn(Optional.empty());

        ResponseEntity<ApplicationDetailResponse> result = controller.approveApplication("APP-NOTFOUND", request);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    void rejectApplication_returnsOkWhenSuccessful() {
        ApplicationDetailResponse rejectedResponse = new ApplicationDetailResponse(
                "APP-123456",
                "test@example.com",
                "John",
                "Doe",
                "1990-01-01",
                "Male",
                "San Francisco",
                List.of("Honest", "Kind"),
                "Looking for connection",
                List.of("photo1.jpg"),
                "rejected",
                "2026-01-15T10:00:00Z",
                "2026-01-16T14:00:00Z",
                "Not suitable",
                "incomplete_profile"
        );
        RejectApplicationRequest request = new RejectApplicationRequest("incomplete_profile", "Not suitable");
        when(adminApplicationService.rejectApplication("APP-123456", "incomplete_profile", "Not suitable"))
                .thenReturn(Optional.of(rejectedResponse));

        ResponseEntity<ApplicationDetailResponse> result = controller.rejectApplication("APP-123456", request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("rejected", result.getBody().status());
    }

    @Test
    void rejectApplication_returnsNotFoundWhenApplicationNotExists() {
        RejectApplicationRequest request = new RejectApplicationRequest("reason", "notes");
        when(adminApplicationService.rejectApplication("APP-NOTFOUND", "reason", "notes"))
                .thenReturn(Optional.empty());

        ResponseEntity<ApplicationDetailResponse> result = controller.rejectApplication("APP-NOTFOUND", request);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }
}
