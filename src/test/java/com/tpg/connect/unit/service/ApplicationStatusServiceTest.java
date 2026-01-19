package com.tpg.connect.unit.service;

import com.tpg.connect.application.model.entity.Application;
import com.tpg.connect.application.model.response.ApplicationStatusResponse;
import com.tpg.connect.application.repository.ApplicationRepository;
import com.tpg.connect.application.service.ApplicationStatusService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApplicationStatusServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    private ApplicationStatusService applicationStatusService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        applicationStatusService = new ApplicationStatusService(applicationRepository);
    }

    @Test
    void getStatus_returnsStatus_whenApplicationExists() {
        Application application = Application.builder()
                .applicationId("APP-123456")
                .email("test@example.com")
                .status("pending")
                .createdAt(Instant.parse("2024-01-15T10:00:00Z"))
                .build();

        when(applicationRepository.findByEmail("test@example.com")).thenReturn(Optional.of(application));

        Optional<ApplicationStatusResponse> result = applicationStatusService.getStatus("test@example.com");

        assertTrue(result.isPresent());
        assertEquals("APP-123456", result.get().applicationId());
        assertEquals("pending", result.get().status());
        assertEquals("2024-01-15T10:00:00Z", result.get().submittedAt());
    }

    @Test
    void getStatus_returnsEmpty_whenApplicationNotFound() {
        when(applicationRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        Optional<ApplicationStatusResponse> result = applicationStatusService.getStatus("notfound@example.com");

        assertFalse(result.isPresent());
    }

    @Test
    void getStatus_normalizesEmail_toLowercase() {
        Application application = Application.builder()
                .applicationId("APP-123456")
                .email("test@example.com")
                .status("approved")
                .createdAt(Instant.now())
                .build();

        when(applicationRepository.findByEmail("TEST@EXAMPLE.COM")).thenReturn(Optional.of(application));

        Optional<ApplicationStatusResponse> result = applicationStatusService.getStatus("TEST@EXAMPLE.COM");

        assertTrue(result.isPresent());
        verify(applicationRepository).findByEmail("TEST@EXAMPLE.COM");
    }

    @Test
    void getStatus_returnsApproved_whenStatusIsApproved() {
        Application application = Application.builder()
                .applicationId("APP-789")
                .email("approved@example.com")
                .status("approved")
                .createdAt(Instant.now())
                .build();

        when(applicationRepository.findByEmail("approved@example.com")).thenReturn(Optional.of(application));

        Optional<ApplicationStatusResponse> result = applicationStatusService.getStatus("approved@example.com");

        assertTrue(result.isPresent());
        assertEquals("approved", result.get().status());
    }

    @Test
    void getStatus_returnsRejected_whenStatusIsRejected() {
        Application application = Application.builder()
                .applicationId("APP-999")
                .email("rejected@example.com")
                .status("rejected")
                .createdAt(Instant.now())
                .build();

        when(applicationRepository.findByEmail("rejected@example.com")).thenReturn(Optional.of(application));

        Optional<ApplicationStatusResponse> result = applicationStatusService.getStatus("rejected@example.com");

        assertTrue(result.isPresent());
        assertEquals("rejected", result.get().status());
    }
}

