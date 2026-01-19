package com.tpg.connect.unit.factory;

import com.tpg.connect.application.factory.ApplicationFactory;
import com.tpg.connect.application.model.entity.Application;
import com.tpg.connect.application.model.request.ApplicationSubmissionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationFactoryTest {

    private ApplicationFactory applicationFactory;

    @BeforeEach
    void setUp() {
        applicationFactory = new ApplicationFactory();
    }

    @Test
    void create_createsApplicationWithPendingStatus() {
        ApplicationSubmissionRequest request = new ApplicationSubmissionRequest(
                "John",
                "Doe",
                "1990-01-01",
                "test@example.com",
                "password",
                "Male",
                "San Francisco",
                List.of("Honest", "Kind"),
                "Looking for connection",
                null
        );
        Instant createdAt = Instant.now();

        Application result = applicationFactory.create(
                request,
                "APP-123",
                "hashedPassword",
                List.of("photo1.jpg"),
                createdAt
        );

        assertNotNull(result);
        assertEquals("APP-123", result.applicationId());
        assertEquals("test@example.com", result.email());
        assertEquals("hashedPassword", result.hashedPassword());
        assertEquals("pending", result.status());
        assertEquals(createdAt, result.createdAt());
    }

    @Test
    void createApproved_createsApprovedApplicationWithReviewedAt() {
        Application original = Application.builder()
                .applicationId("APP-123")
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

        Application result = applicationFactory.createApproved(original, "Great profile");

        assertNotNull(result);
        assertEquals("APP-123", result.applicationId());
        assertEquals("test@example.com", result.email());
        assertEquals("approved", result.status());
        assertEquals("Great profile", result.reviewNotes());
        assertNotNull(result.reviewedAt());
        assertNull(result.rejectionReason());
    }

    @Test
    void createApproved_preservesOriginalFields() {
        Instant originalCreatedAt = Instant.now().minusSeconds(3600);
        Application original = Application.builder()
                .applicationId("APP-456")
                .email("user@example.com")
                .hashedPassword("pass123")
                .firstName("Jane")
                .lastName("Smith")
                .dateOfBirth("1995-05-15")
                .gender("Female")
                .location("New York")
                .bestQualities(List.of("Creative"))
                .reasonForJoining("Meeting people")
                .photoUrls(List.of("photo2.jpg", "photo3.jpg"))
                .status("pending")
                .createdAt(originalCreatedAt)
                .build();

        Application result = applicationFactory.createApproved(original, "Notes");

        assertEquals("user@example.com", result.email());
        assertEquals("Jane", result.firstName());
        assertEquals("Smith", result.lastName());
        assertEquals(originalCreatedAt, result.createdAt());
        assertEquals(List.of("photo2.jpg", "photo3.jpg"), result.photoUrls());
    }

    @Test
    void createRejected_createsRejectedApplicationWithReason() {
        Application original = Application.builder()
                .applicationId("APP-789")
                .email("test@example.com")
                .hashedPassword("hashedPassword")
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth("1990-01-01")
                .gender("Male")
                .location("San Francisco")
                .bestQualities(List.of("Honest"))
                .reasonForJoining("Testing")
                .photoUrls(List.of())
                .status("pending")
                .createdAt(Instant.now())
                .build();

        Application result = applicationFactory.createRejected(original, "incomplete_profile", "Need more photos");

        assertNotNull(result);
        assertEquals("APP-789", result.applicationId());
        assertEquals("rejected", result.status());
        assertEquals("incomplete_profile", result.rejectionReason());
        assertEquals("Need more photos", result.reviewNotes());
        assertNotNull(result.reviewedAt());
    }

    @Test
    void createRejected_preservesOriginalFields() {
        Instant originalCreatedAt = Instant.now().minusSeconds(7200);
        Application original = Application.builder()
                .applicationId("APP-999")
                .email("reject@example.com")
                .hashedPassword("pass")
                .firstName("Bob")
                .lastName("Brown")
                .dateOfBirth("1988-03-20")
                .gender("Male")
                .location("Chicago")
                .bestQualities(List.of("Funny"))
                .reasonForJoining("Fun")
                .photoUrls(List.of("bob.jpg"))
                .status("pending")
                .createdAt(originalCreatedAt)
                .build();

        Application result = applicationFactory.createRejected(original, "reason", "notes");

        assertEquals("reject@example.com", result.email());
        assertEquals("Bob", result.firstName());
        assertEquals("Brown", result.lastName());
        assertEquals(originalCreatedAt, result.createdAt());
    }
}

