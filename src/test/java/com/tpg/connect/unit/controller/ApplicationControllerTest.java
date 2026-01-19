package com.tpg.connect.unit.controller;

import com.tpg.connect.application.controller.api.ApplicationController;
import com.tpg.connect.application.model.request.ApplicationStatusRequest;
import com.tpg.connect.application.model.request.ApplicationSubmissionRequest;
import com.tpg.connect.application.model.response.ApplicationStatusResponse;
import com.tpg.connect.application.model.response.ApplicationSubmissionResponse;
import com.tpg.connect.application.model.response.ApplicationSubmissionResult;
import com.tpg.connect.application.service.ApplicationService;
import com.tpg.connect.application.service.ApplicationStatusService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ApplicationControllerTest {

    @Mock
    private ApplicationService applicationService;

    @Mock
    private ApplicationStatusService applicationStatusService;

    private ApplicationController applicationController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        applicationController = new ApplicationController(applicationService, applicationStatusService);
    }

    @Test
    void submitApplication_returnsOk_whenSubmissionSuccessful() {
        ApplicationSubmissionRequest request = new ApplicationSubmissionRequest(
                "test@example.com",
                "password",
                "John",
                "Doe",
                "1990-01-01",
                "Male",
                "San Francisco",
                List.of("Honest"),
                "Looking for connection",
                null
        );

        ApplicationSubmissionResult result = new ApplicationSubmissionResult(
                "APP-123456",
                "Application submitted successfully"
        );

        when(applicationService.submitApplication(any())).thenReturn(result);

        ResponseEntity<ApplicationSubmissionResponse> response = applicationController.submitApplication(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("APP-123456", response.getBody().applicationId());
        assertEquals("Application submitted successfully", response.getBody().message());
    }

    @Test
    void getApplicationStatus_returnsOk_whenApplicationExists() {
        ApplicationStatusRequest request = new ApplicationStatusRequest("test@example.com");
        ApplicationStatusResponse statusResponse = new ApplicationStatusResponse(
                "APP-123456",
                "pending",
                "2024-01-15T10:00:00Z"
        );

        when(applicationStatusService.getStatus("test@example.com"))
                .thenReturn(Optional.of(statusResponse));

        ResponseEntity<ApplicationStatusResponse> response = applicationController.getApplicationStatus(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("APP-123456", response.getBody().applicationId());
        assertEquals("pending", response.getBody().status());
    }

    @Test
    void getApplicationStatus_returnsNotFound_whenApplicationNotExists() {
        ApplicationStatusRequest request = new ApplicationStatusRequest("notfound@example.com");

        when(applicationStatusService.getStatus("notfound@example.com"))
                .thenReturn(Optional.empty());

        ResponseEntity<ApplicationStatusResponse> response = applicationController.getApplicationStatus(request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }
}

