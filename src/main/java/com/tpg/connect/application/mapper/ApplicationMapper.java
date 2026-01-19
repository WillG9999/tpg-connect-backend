package com.tpg.connect.application.mapper;

import com.google.cloud.firestore.DocumentSnapshot;
import com.tpg.connect.application.model.entity.Application;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ApplicationMapper {

    @SuppressWarnings("unchecked")
    public Application documentToApplication(DocumentSnapshot doc) {
        String createdAtStr = doc.getString("createdAt");
        String reviewedAtStr = doc.getString("reviewedAt");

        return Application.builder()
                .applicationId(doc.getString("applicationId"))
                .email(doc.getString("email"))
                .hashedPassword(doc.getString("hashedPassword"))
                .firstName(doc.getString("firstName"))
                .lastName(doc.getString("lastName"))
                .dateOfBirth(doc.getString("dateOfBirth"))
                .gender(doc.getString("gender"))
                .location(doc.getString("location"))
                .bestQualities((List<String>) doc.get("bestQualities"))
                .reasonForJoining(doc.getString("reasonForJoining"))
                .photoUrls((List<String>) doc.get("photoUrls"))
                .status(doc.getString("status"))
                .createdAt(createdAtStr != null ? Instant.parse(createdAtStr) : null)
                .reviewedAt(reviewedAtStr != null ? Instant.parse(reviewedAtStr) : null)
                .reviewNotes(doc.getString("reviewNotes"))
                .rejectionReason(doc.getString("rejectionReason"))
                .build();
    }

    public Map<String, Object> applicationToDocument(Application application) {
        Map<String, Object> data = new HashMap<>();
        data.put("applicationId", application.applicationId());
        data.put("email", application.email().toLowerCase().trim());
        data.put("hashedPassword", application.hashedPassword());
        data.put("firstName", application.firstName());
        data.put("lastName", application.lastName());
        data.put("dateOfBirth", application.dateOfBirth());
        data.put("gender", application.gender());
        data.put("location", application.location());
        data.put("bestQualities", application.bestQualities());
        data.put("reasonForJoining", application.reasonForJoining());
        data.put("photoUrls", application.photoUrls());
        data.put("status", application.status());
        data.put("createdAt", application.createdAt() != null ? application.createdAt().toString() : null);
        data.put("reviewedAt", application.reviewedAt() != null ? application.reviewedAt().toString() : null);
        data.put("reviewNotes", application.reviewNotes());
        data.put("rejectionReason", application.rejectionReason());
        return data;
    }
}
