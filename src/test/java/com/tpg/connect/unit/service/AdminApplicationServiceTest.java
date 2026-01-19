package com.tpg.connect.unit.service;

import com.tpg.connect.admin.factory.ApprovedUserFactory;
import com.tpg.connect.admin.model.response.ApplicationDetailResponse;
import com.tpg.connect.admin.model.response.ApplicationsPageResponse;
import com.tpg.connect.admin.service.AdminApplicationService;
import com.tpg.connect.application.factory.ApplicationFactory;
import com.tpg.connect.application.model.entity.Application;
import com.tpg.connect.application.repository.ApplicationRepository;
import com.tpg.connect.external.email.client.EmailClient;
import com.tpg.connect.profile.factory.ProfileFactory;
import com.tpg.connect.profile.model.entity.UserProfile;
import com.tpg.connect.profile.repository.ProfileRepository;
import com.tpg.connect.user_registration.model.entity.RegisteredUser;
import com.tpg.connect.user_registration.repository.RegisterUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AdminApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private RegisterUserRepository userRepository;

    @Mock
    private ApplicationFactory applicationFactory;

    @Mock
    private ApprovedUserFactory approvedUserFactory;

    @Mock
    private EmailClient emailClient;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private ProfileFactory profileFactory;

    private AdminApplicationService adminApplicationService;

    private Application testApplication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adminApplicationService = new AdminApplicationService(
                applicationRepository,
                userRepository,
                applicationFactory,
                approvedUserFactory,
                emailClient,
                profileRepository,
                profileFactory
        );

        testApplication = Application.builder()
                .applicationId("APP-123456")
                .email("test@example.com")
                .hashedPassword("hashedPassword")
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth("1990-01-01")
                .gender("Male")
                .location("San Francisco")
                .bestQualities(List.of("Honest", "Kind"))
                .reasonForJoining("Looking for connection")
                .photoUrls(List.of("photo1.jpg"))
                .status("pending")
                .createdAt(Instant.now())
                .build();
    }

    @Test
    void getAllApplications_returnsPaginatedList() {
        when(applicationRepository.findAll()).thenReturn(List.of(testApplication));

        ApplicationsPageResponse response = adminApplicationService.getAllApplications(0, 10);

        assertNotNull(response);
        assertEquals(1, response.totalElements());
        assertEquals(1, response.applications().size());
        assertEquals(0, response.page());
        assertEquals(10, response.size());
        assertTrue(response.first());
        assertTrue(response.last());
        verify(applicationRepository).findAll();
    }

    @Test
    void getPendingApplications_returnsPaginatedList() {
        when(applicationRepository.findByStatus("pending")).thenReturn(List.of(testApplication));

        ApplicationsPageResponse response = adminApplicationService.getPendingApplications(0, 10);

        assertNotNull(response);
        assertEquals(1, response.totalElements());
        assertEquals(1, response.applications().size());
        assertEquals("APP-123456", response.applications().getFirst().applicationId());
        verify(applicationRepository).findByStatus("pending");
    }

    @Test
    void getPendingApplications_returnsEmptyListWhenNoPendingApplications() {
        when(applicationRepository.findByStatus("pending")).thenReturn(List.of());

        ApplicationsPageResponse response = adminApplicationService.getPendingApplications(0, 10);

        assertNotNull(response);
        assertEquals(0, response.totalElements());
        assertTrue(response.applications().isEmpty());
    }

    @Test
    void getApplicationsByStatus_returnsPaginatedListForStatus() {
        Application approvedApp = Application.builder()
                .applicationId("APP-APPROVED")
                .status("approved")
                .build();
        when(applicationRepository.findByStatus("approved")).thenReturn(List.of(approvedApp));

        ApplicationsPageResponse response = adminApplicationService.getApplicationsByStatus("approved", 0, 10);

        assertNotNull(response);
        assertEquals(1, response.totalElements());
        verify(applicationRepository).findByStatus("approved");
    }

    @Test
    void getPendingApplications_paginatesCorrectly() {
        List<Application> manyApps = List.of(
                testApplication,
                Application.builder().applicationId("APP-2").status("pending").createdAt(Instant.now()).build(),
                Application.builder().applicationId("APP-3").status("pending").createdAt(Instant.now()).build()
        );
        when(applicationRepository.findByStatus("pending")).thenReturn(manyApps);

        ApplicationsPageResponse page0 = adminApplicationService.getPendingApplications(0, 2);
        assertEquals(2, page0.applications().size());
        assertEquals(3, page0.totalElements());
        assertEquals(2, page0.totalPages());
        assertTrue(page0.first());
        assertFalse(page0.last());

        ApplicationsPageResponse page1 = adminApplicationService.getPendingApplications(1, 2);
        assertEquals(1, page1.applications().size());
        assertFalse(page1.first());
        assertTrue(page1.last());
    }

    @Test
    void getApplication_returnsApplicationWhenFound() {
        when(applicationRepository.findByApplicationId("APP-123456")).thenReturn(Optional.of(testApplication));

        Optional<ApplicationDetailResponse> result = adminApplicationService.getApplication("APP-123456");

        assertTrue(result.isPresent());
        assertEquals("APP-123456", result.get().applicationId());
        assertEquals("test@example.com", result.get().email());
    }

    @Test
    void getApplication_returnsEmptyWhenNotFound() {
        when(applicationRepository.findByApplicationId("APP-NOTFOUND")).thenReturn(Optional.empty());

        Optional<ApplicationDetailResponse> result = adminApplicationService.getApplication("APP-NOTFOUND");

        assertFalse(result.isPresent());
    }

    @Test
    void approveApplication_successfullyApprovesAndCreatesUser() {
        Application approvedApp = Application.builder()
                .applicationId("APP-123456")
                .email("test@example.com")
                .status("approved")
                .createdAt(testApplication.createdAt())
                .reviewedAt(Instant.now())
                .reviewNotes("Approved")
                .build();

        RegisteredUser newUser = RegisteredUser.builder()
                .connectId(12345L)
                .email("test@example.com")
                .build();

        UserProfile profile = UserProfile.builder()
                .connectId(12345L)
                .email("test@example.com")
                .build();

        when(applicationRepository.findByApplicationId("APP-123456")).thenReturn(Optional.of(testApplication));
        when(approvedUserFactory.createFromApplication(testApplication)).thenReturn(newUser);
        when(userRepository.saveUserWithApplicationId(newUser, "APP-123456")).thenReturn(true);
        when(profileFactory.createFromApplication(testApplication, 12345L)).thenReturn(profile);
        when(profileRepository.createProfile(profile)).thenReturn(true);
        when(applicationFactory.createApproved(testApplication, "Great profile")).thenReturn(approvedApp);
        when(applicationRepository.updateApplication(approvedApp)).thenReturn(true);
        when(emailClient.sendApplicationApprovalEmail(anyString(), anyString())).thenReturn(true);

        Optional<ApplicationDetailResponse> result = adminApplicationService.approveApplication("APP-123456", "Great profile");

        assertTrue(result.isPresent());
        assertEquals("approved", result.get().status());
        verify(userRepository).saveUserWithApplicationId(newUser, "APP-123456");
        verify(profileRepository).createProfile(profile);
        verify(applicationRepository).updateApplication(approvedApp);
        verify(emailClient).sendApplicationApprovalEmail("test@example.com", "John");
    }

    @Test
    void approveApplication_returnsEmptyWhenApplicationNotFound() {
        when(applicationRepository.findByApplicationId("APP-NOTFOUND")).thenReturn(Optional.empty());

        Optional<ApplicationDetailResponse> result = adminApplicationService.approveApplication("APP-NOTFOUND", "Notes");

        assertFalse(result.isPresent());
        verify(userRepository, never()).saveUser(any());
    }

    @Test
    void approveApplication_returnsEmptyWhenApplicationAlreadyProcessed() {
        Application alreadyApproved = Application.builder()
                .applicationId("APP-123456")
                .status("approved")
                .build();

        when(applicationRepository.findByApplicationId("APP-123456")).thenReturn(Optional.of(alreadyApproved));

        Optional<ApplicationDetailResponse> result = adminApplicationService.approveApplication("APP-123456", "Notes");

        assertFalse(result.isPresent());
        verify(userRepository, never()).saveUserWithApplicationId(any(), anyString());
    }

    @Test
    void approveApplication_returnsEmptyWhenUserCreationFails() {
        RegisteredUser newUser = RegisteredUser.builder()
                .connectId(12345L)
                .email("test@example.com")
                .build();

        when(applicationRepository.findByApplicationId("APP-123456")).thenReturn(Optional.of(testApplication));
        when(approvedUserFactory.createFromApplication(testApplication)).thenReturn(newUser);
        when(userRepository.saveUserWithApplicationId(newUser, "APP-123456")).thenReturn(false);

        Optional<ApplicationDetailResponse> result = adminApplicationService.approveApplication("APP-123456", "Notes");

        assertFalse(result.isPresent());
        verify(applicationRepository, never()).updateApplication(any());
        verify(emailClient, never()).sendApplicationApprovalEmail(anyString(), anyString());
    }

    @Test
    void rejectApplication_successfullyRejectsApplication() {
        Application rejectedApp = Application.builder()
                .applicationId("APP-123456")
                .email("test@example.com")
                .status("rejected")
                .createdAt(testApplication.createdAt())
                .reviewedAt(Instant.now())
                .reviewNotes("Not suitable")
                .rejectionReason("incomplete_profile")
                .build();

        when(applicationRepository.findByApplicationId("APP-123456")).thenReturn(Optional.of(testApplication));
        when(applicationFactory.createRejected(testApplication, "incomplete_profile", "Not suitable")).thenReturn(rejectedApp);
        when(applicationRepository.updateApplication(rejectedApp)).thenReturn(true);

        Optional<ApplicationDetailResponse> result = adminApplicationService.rejectApplication("APP-123456", "incomplete_profile", "Not suitable");

        assertTrue(result.isPresent());
        assertEquals("rejected", result.get().status());
        verify(applicationRepository).updateApplication(rejectedApp);
        verify(emailClient, never()).sendApplicationApprovalEmail(anyString(), anyString());
    }

    @Test
    void rejectApplication_returnsEmptyWhenApplicationNotFound() {
        when(applicationRepository.findByApplicationId("APP-NOTFOUND")).thenReturn(Optional.empty());

        Optional<ApplicationDetailResponse> result = adminApplicationService.rejectApplication("APP-NOTFOUND", "reason", "notes");

        assertFalse(result.isPresent());
    }

    @Test
    void rejectApplication_returnsEmptyWhenApplicationAlreadyProcessed() {
        Application alreadyRejected = Application.builder()
                .applicationId("APP-123456")
                .status("rejected")
                .build();

        when(applicationRepository.findByApplicationId("APP-123456")).thenReturn(Optional.of(alreadyRejected));

        Optional<ApplicationDetailResponse> result = adminApplicationService.rejectApplication("APP-123456", "reason", "notes");

        assertFalse(result.isPresent());
        verify(applicationRepository, never()).updateApplication(any());
    }
}
