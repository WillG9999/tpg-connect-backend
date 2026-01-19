package com.tpg.connect.application.factory;

import com.tpg.connect.application.model.entity.Application;
import com.tpg.connect.application.model.request.ApplicationSubmissionRequest;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class ApplicationFactory {

    public Application create(
            ApplicationSubmissionRequest request,
            String applicationId,
            String hashedPassword,
            List<String> photoUrls,
            Instant createdAt
    ) {
        return Application.builder()
                .applicationId(applicationId)
                .email(request.email())
                .hashedPassword(hashedPassword)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .dateOfBirth(request.dateOfBirth())
                .gender(request.gender())
                .location(request.location())
                .bestQualities(request.bestQualities())
                .reasonForJoining(request.reasonForJoining())
                .photoUrls(photoUrls)
                .status("pending")
                .createdAt(createdAt)
                .build();
    }

    public Application createApproved(Application original, String reviewNotes) {
        return Application.builder()
                .applicationId(original.applicationId())
                .email(original.email())
                .hashedPassword(original.hashedPassword())
                .firstName(original.firstName())
                .lastName(original.lastName())
                .dateOfBirth(original.dateOfBirth())
                .gender(original.gender())
                .location(original.location())
                .bestQualities(original.bestQualities())
                .reasonForJoining(original.reasonForJoining())
                .photoUrls(original.photoUrls())
                .status("approved")
                .createdAt(original.createdAt())
                .reviewedAt(Instant.now())
                .reviewNotes(reviewNotes)
                .build();
    }

    public Application createRejected(Application original, String rejectionReason, String reviewNotes) {
        return Application.builder()
                .applicationId(original.applicationId())
                .email(original.email())
                .hashedPassword(original.hashedPassword())
                .firstName(original.firstName())
                .lastName(original.lastName())
                .dateOfBirth(original.dateOfBirth())
                .gender(original.gender())
                .location(original.location())
                .bestQualities(original.bestQualities())
                .reasonForJoining(original.reasonForJoining())
                .photoUrls(original.photoUrls())
                .status("rejected")
                .createdAt(original.createdAt())
                .reviewedAt(Instant.now())
                .reviewNotes(reviewNotes)
                .rejectionReason(rejectionReason)
                .build();
    }
}

