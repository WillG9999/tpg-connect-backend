package com.tpg.connect.application.service;

import com.tpg.connect.application.components.AppIdGenerator;
import com.tpg.connect.application.factory.ApplicationFactory;
import com.tpg.connect.application.model.entity.Application;
import com.tpg.connect.application.model.request.ApplicationSubmissionRequest;
import com.tpg.connect.application.model.response.ApplicationSubmissionResult;
import com.tpg.connect.application.repository.ApplicationRepositoryApi;
import com.tpg.connect.common.services.PasswordService;
import com.tpg.connect.common.storage.service.PhotoStorageServiceApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final AppIdGenerator appIdGenerator;
    private final ApplicationRepositoryApi applicationRepository;
    private final PhotoStorageServiceApi photoStorageService;
    private final PasswordService passwordService;
    private final ApplicationFactory applicationFactory;

    public ApplicationSubmissionResult submitApplication(ApplicationSubmissionRequest request) {
        log.info("Processing application submission for: {}", request.email());

        String applicationId = appIdGenerator.generateAppId();
        Instant createdAt = Instant.now();

        String hashedPassword = passwordService.hashPassword(request.password());
        List<String> photoUrls = uploadPhotos(applicationId, request.photos());

        Application application = applicationFactory.create(request, applicationId, hashedPassword, photoUrls, createdAt);

        boolean saved = applicationRepository.saveApplication(application);

        if (!saved) {
            log.error("Failed to save application for: {}", request.email());
            throw new RuntimeException("Failed to submit application");
        }

        log.info("Application submitted successfully - applicationId: {}", applicationId);
        return new ApplicationSubmissionResult(applicationId, "Application submitted successfully");
    }

    private List<String> uploadPhotos(String applicationId, List<MultipartFile> photos) {
        List<String> photoUrls = new ArrayList<>();

        if (photos == null || photos.isEmpty()) {
            log.debug("No photos to upload for application: {}", applicationId);
            return photoUrls;
        }

        for (MultipartFile photo : photos) {
            if (photo != null && !photo.isEmpty()) {
                try {
                    String photoUrl = photoStorageService.uploadPhoto(applicationId, photo);
                    photoUrls.add(photoUrl);
                    log.debug("Uploaded photo for application {}: {}", applicationId, photoUrl);
                } catch (Exception e) {
                    log.error("Failed to upload photo for application: {}", applicationId, e);
                }
            }
        }

        return photoUrls;
    }
}
