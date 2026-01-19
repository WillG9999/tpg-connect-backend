package com.tpg.connect.application.repository;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.tpg.connect.application.mapper.ApplicationMapper;
import com.tpg.connect.application.model.entity.Application;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.*;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ApplicationRepository implements ApplicationRepositoryApi {

    private static final String APPLICATIONS_COLLECTION = "applications";
    private static final String EMAIL_LOOKUP_COLLECTION = "emailToAppIdLookup";

    private final Firestore firestore;
    private final ApplicationMapper applicationMapper;

    @Override
    public boolean saveApplication(Application application) {
        String normalizedEmail = application.email().toLowerCase().trim();

        try {
            firestore.runTransaction(transaction -> {
                var emailLookupDoc = firestore.collection(EMAIL_LOOKUP_COLLECTION).document(normalizedEmail);

                if (transaction.get(emailLookupDoc).get().exists())
                    throw new RuntimeException("Email already registered: " + normalizedEmail);

                var applicationDoc = firestore.collection(APPLICATIONS_COLLECTION).document(application.applicationId());

                transaction.create(applicationDoc, applicationMapper.applicationToDocument(application));
                transaction.create(emailLookupDoc, Map.of("applicationId", application.applicationId()));

                return null;
            }).get();

            log.info("Application saved successfully - applicationId: {}",
                    application.applicationId());
            return true;

        } catch (Exception e) {
            log.error("Failed to save application - applicationId: {}, error: {}",
                    application.applicationId(), e.getMessage());
            return false;
        }
    }

    public Optional<Application> findByEmail(String email) {
        String normalizedEmail = email.toLowerCase().trim();

        try {
            var emailLookupDoc = firestore.collection(EMAIL_LOOKUP_COLLECTION).document(normalizedEmail).get().get();

            if (!emailLookupDoc.exists()) {
                log.debug("No application found for email: {}", normalizedEmail);
                return Optional.empty();
            }

            String applicationId = emailLookupDoc.getString("applicationId");
            if (applicationId == null) {
                log.error("Application lookup has null applicationId for email: {}", normalizedEmail);
                return Optional.empty();
            }
            var applicationDoc = firestore.collection(APPLICATIONS_COLLECTION).document(applicationId).get().get();

            if (!applicationDoc.exists()) {
                log.error("Application lookup inconsistency - email exists but application missing: {}", applicationId);
                return Optional.empty();
            }

            return Optional.of(applicationMapper.documentToApplication(applicationDoc));

        } catch (Exception e) {
            log.error("Failed to find application by email: {}", normalizedEmail, e);
            return Optional.empty();
        }
    }

    public Optional<Application> findByApplicationId(String applicationId) {
        try {
            var applicationDoc = firestore.collection(APPLICATIONS_COLLECTION).document(applicationId).get().get();

            if (!applicationDoc.exists()) {
                log.debug("No application found for applicationId: {}", applicationId);
                return Optional.empty();
            }

            return Optional.of(applicationMapper.documentToApplication(applicationDoc));

        } catch (Exception e) {
            log.error("Failed to find application by applicationId: {}", applicationId, e);
            return Optional.empty();
        }
    }

    public List<Application> findByStatus(String status) {
        try {
            var querySnapshot = firestore.collection(APPLICATIONS_COLLECTION)
                    .whereEqualTo("status", status)
                    .get()
                    .get();

            List<Application> applications = new ArrayList<>();
            for (QueryDocumentSnapshot doc : querySnapshot.getDocuments()) {
                applications.add(applicationMapper.documentToApplication(doc));
            }

            log.info("Found {} applications with status: {}", applications.size(), status);
            return applications;

        } catch (Exception e) {
            log.error("Failed to find applications by status: {}", status, e);
            return Collections.emptyList();
        }
    }

    public List<Application> findAll() {
        try {
            var querySnapshot = firestore.collection(APPLICATIONS_COLLECTION)
                    .get()
                    .get();

            List<Application> applications = new ArrayList<>();
            for (QueryDocumentSnapshot doc : querySnapshot.getDocuments()) {
                applications.add(applicationMapper.documentToApplication(doc));
            }

            log.info("Found {} total applications", applications.size());
            return applications;

        } catch (Exception e) {
            log.error("Failed to find all applications", e);
            return Collections.emptyList();
        }
    }

    public boolean updateApplication(Application application) {
        try {
            firestore.collection(APPLICATIONS_COLLECTION)
                    .document(application.applicationId())
                    .set(applicationMapper.applicationToDocument(application))
                    .get();

            log.info("Application updated successfully - applicationId: {}", application.applicationId());
            return true;

        } catch (Exception e) {
            log.error("Failed to update application - applicationId: {}, error: {}",
                    application.applicationId(), e.getMessage());
            return false;
        }
    }
}

