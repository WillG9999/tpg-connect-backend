package com.tpg.connect.admin.service;

import com.tpg.connect.admin.factory.ApprovedUserFactory;
import com.tpg.connect.admin.model.response.ApplicationDetailResponse;
import com.tpg.connect.admin.model.response.ApplicationsPageResponse;
import com.tpg.connect.application.factory.ApplicationFactory;
import com.tpg.connect.application.model.entity.Application;
import com.tpg.connect.application.repository.ApplicationRepository;
import com.tpg.connect.external.email.client.EmailClient;
import com.tpg.connect.profile.factory.ProfileFactory;
import com.tpg.connect.profile.model.entity.UserProfile;
import com.tpg.connect.profile.repository.ProfileRepository;
import com.tpg.connect.user_registration.model.entity.RegisteredUser;
import com.tpg.connect.user_registration.repository.RegisterUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminApplicationService {

    private final ApplicationRepository applicationRepository;
    private final RegisterUserRepository userRepository;
    private final ApplicationFactory applicationFactory;
    private final ApprovedUserFactory approvedUserFactory;
    private final EmailClient emailClient;
    private final ProfileRepository profileRepository;
    private final ProfileFactory profileFactory;

    public ApplicationsPageResponse getAllApplications(int page, int size) {
        log.info("Fetching all applications - page: {}, size: {}", page, size);
        return getPagedApplications(null, page, size);
    }

    public ApplicationsPageResponse getPendingApplications(int page, int size) {
        log.info("Fetching pending applications - page: {}, size: {}", page, size);
        return getPagedApplications("pending", page, size);
    }

    public ApplicationsPageResponse getApplicationsByStatus(String status, int page, int size) {
        log.info("Fetching applications with status: {} - page: {}, size: {}", status, page, size);
        return getPagedApplications(status, page, size);
    }

    private ApplicationsPageResponse getPagedApplications(String status, int page, int size) {
        List<Application> allApps = status == null
                ? applicationRepository.findAll()
                : applicationRepository.findByStatus(status);

        long totalElements = allApps.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, allApps.size());

        List<ApplicationDetailResponse> pagedApps = fromIndex < allApps.size()
                ? allApps.subList(fromIndex, toIndex).stream()
                        .map(this::toDetailResponse)
                        .toList()
                : List.of();

        return new ApplicationsPageResponse(
                pagedApps,
                page,
                size,
                totalElements,
                totalPages,
                page == 0,
                page >= totalPages - 1
        );
    }

    public Optional<ApplicationDetailResponse> getApplication(String applicationId) {
        log.info("Fetching application: {}", applicationId);
        return applicationRepository.findByApplicationId(applicationId)
                .map(this::toDetailResponse);
    }

    public Optional<ApplicationDetailResponse> approveApplication(String applicationId, String notes) {
        log.info("Approving application: {}", applicationId);

        Optional<Application> applicationOpt = applicationRepository.findByApplicationId(applicationId);
        if (applicationOpt.isEmpty()) {
            return Optional.empty();
        }

        Application application = applicationOpt.get();

        if (!"pending".equals(application.status())) {
            log.warn("Application {} already processed with status: {}", applicationId, application.status());
            return Optional.empty();
        }

        RegisteredUser newUser = approvedUserFactory.createFromApplication(application);

        boolean userSaved = userRepository.saveUserWithApplicationId(newUser, applicationId);
        if (!userSaved) {
            log.error("Failed to create user for approved application: {}", applicationId);
            return Optional.empty();
        }

        UserProfile profile = profileFactory.createFromApplication(application, newUser.connectId());
        boolean profileCreated = profileRepository.createProfile(profile);
        if (!profileCreated) {
            log.warn("Failed to create profile for connectId: {}", newUser.connectId());
        }

        Application updatedApplication = applicationFactory.createApproved(application, notes);
        applicationRepository.updateApplication(updatedApplication);

        sendApprovalEmail(application.email(), application.firstName());

        log.info("Application approved, user and profile created: {}", applicationId);
        return Optional.of(toDetailResponse(updatedApplication));
    }

    public Optional<ApplicationDetailResponse> rejectApplication(String applicationId, String reason, String notes) {
        log.info("Rejecting application: {}", applicationId);

        Optional<Application> applicationOpt = applicationRepository.findByApplicationId(applicationId);
        if (applicationOpt.isEmpty()) {
            return Optional.empty();
        }

        Application application = applicationOpt.get();

        if (!"pending".equals(application.status())) {
            log.warn("Application {} already processed with status: {}", applicationId, application.status());
            return Optional.empty();
        }

        Application updatedApplication = applicationFactory.createRejected(application, reason, notes);
        applicationRepository.updateApplication(updatedApplication);

        log.info("Application rejected: {}", applicationId);
        return Optional.of(toDetailResponse(updatedApplication));
    }

    private void sendApprovalEmail(String email, String firstName) {
        emailClient.sendApplicationApprovalEmail(email, firstName);
    }

    private ApplicationDetailResponse toDetailResponse(Application app) {
        return new ApplicationDetailResponse(
                app.applicationId(),
                app.email(),
                app.firstName(),
                app.lastName(),
                app.dateOfBirth(),
                app.gender(),
                app.location(),
                app.bestQualities(),
                app.reasonForJoining(),
                app.photoUrls(),
                app.status(),
                app.createdAt() != null ? app.createdAt().toString() : null,
                app.reviewedAt() != null ? app.reviewedAt().toString() : null,
                app.reviewNotes(),
                app.rejectionReason()
        );
    }
}
