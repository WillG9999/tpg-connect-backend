package com.tpg.connect.unit.service;

import com.tpg.connect.application.components.AppIdGenerator;
import com.tpg.connect.application.factory.ApplicationFactory;
import com.tpg.connect.application.model.entity.Application;
import com.tpg.connect.application.model.request.ApplicationSubmissionRequest;
import com.tpg.connect.application.model.response.ApplicationSubmissionResult;
import com.tpg.connect.application.repository.ApplicationRepositoryApi;
import com.tpg.connect.application.service.ApplicationService;
import com.tpg.connect.common.services.PasswordService;
import com.tpg.connect.common.storage.service.PhotoStorageServiceApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ApplicationServiceTest {

    @Mock
    private AppIdGenerator appIdGenerator;

    @Mock
    private ApplicationRepositoryApi applicationRepository;

    @Mock
    private PhotoStorageServiceApi photoStorageService;

    @Mock
    private PasswordService passwordService;

    @Mock
    private ApplicationFactory applicationFactory;

    private ApplicationService applicationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        applicationService = new ApplicationService(
                appIdGenerator,
                applicationRepository,
                photoStorageService,
                passwordService,
                applicationFactory
        );
    }

    @Test
    void submitApplication_returnsSuccess_whenApplicationSaved() {
        ApplicationSubmissionRequest request = new ApplicationSubmissionRequest(
                "test@example.com",
                "password123",
                "John",
                "Doe",
                "1990-01-01",
                "Male",
                "San Francisco",
                List.of("Honest", "Kind"),
                "Looking for connection",
                null
        );

        Application mockApplication = Application.builder()
                .applicationId("APP-123456")
                .email("test@example.com")
                .build();

        when(appIdGenerator.generateAppId()).thenReturn("APP-123456");
        when(passwordService.hashPassword(anyString())).thenReturn("hashedPassword");
        when(applicationFactory.create(any(), anyString(), anyString(), anyList(), any()))
                .thenReturn(mockApplication);
        when(applicationRepository.saveApplication(any())).thenReturn(true);

        ApplicationSubmissionResult result = applicationService.submitApplication(request);

        assertNotNull(result);
        assertEquals("APP-123456", result.applicationId());
        assertEquals("Application submitted successfully", result.message());
        verify(applicationRepository).saveApplication(any());
    }

    @Test
    void submitApplication_throwsException_whenSaveFails() {
        ApplicationSubmissionRequest request = new ApplicationSubmissionRequest(
                "test@example.com",
                "password123",
                "John",
                "Doe",
                "1990-01-01",
                "Male",
                "San Francisco",
                List.of("Honest"),
                "Looking for connection",
                null
        );

        Application mockApplication = Application.builder()
                .applicationId("APP-123456")
                .email("test@example.com")
                .build();

        when(appIdGenerator.generateAppId()).thenReturn("APP-123456");
        when(passwordService.hashPassword(anyString())).thenReturn("hashedPassword");
        when(applicationFactory.create(any(), anyString(), anyString(), anyList(), any()))
                .thenReturn(mockApplication);
        when(applicationRepository.saveApplication(any())).thenReturn(false);

        assertThrows(RuntimeException.class, () -> applicationService.submitApplication(request));
    }

    @Test
    void submitApplication_uploadsPhotos_whenPhotosProvided() {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);

        ApplicationSubmissionRequest request = new ApplicationSubmissionRequest(
                "test@example.com",
                "password123",
                "John",
                "Doe",
                "1990-01-01",
                "Male",
                "San Francisco",
                List.of("Honest"),
                "Looking for connection",
                List.of(mockFile)
        );

        Application mockApplication = Application.builder()
                .applicationId("APP-123456")
                .email("test@example.com")
                .build();

        when(appIdGenerator.generateAppId()).thenReturn("APP-123456");
        when(passwordService.hashPassword(anyString())).thenReturn("hashedPassword");
        when(photoStorageService.uploadPhoto(anyString(), any())).thenReturn("https://storage.com/photo1.jpg");
        when(applicationFactory.create(any(), anyString(), anyString(), anyList(), any()))
                .thenReturn(mockApplication);
        when(applicationRepository.saveApplication(any())).thenReturn(true);

        applicationService.submitApplication(request);

        verify(photoStorageService).uploadPhoto(eq("APP-123456"), any());
    }
}

