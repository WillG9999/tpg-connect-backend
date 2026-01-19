package com.tpg.connect.unit.factory;

import com.tpg.connect.admin.factory.ApprovedUserFactory;
import com.tpg.connect.application.model.entity.Application;
import com.tpg.connect.user_registration.components.ConnectIdGenerator;
import com.tpg.connect.user_registration.model.entity.RegisteredUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class ApprovedUserFactoryTest {

    @Mock
    private ConnectIdGenerator connectIdGenerator;

    private ApprovedUserFactory approvedUserFactory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        approvedUserFactory = new ApprovedUserFactory(connectIdGenerator);
    }

    @Test
    void createFromApplication_createsUserWithCorrectFields() {
        Application application = Application.builder()
                .applicationId("APP-123456")
                .email("test@example.com")
                .hashedPassword("hashedPassword123")
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

        when(connectIdGenerator.generateConnectId()).thenReturn(12345L);

        RegisteredUser result = approvedUserFactory.createFromApplication(application);

        assertNotNull(result);
        assertEquals(12345L, result.connectId());
        assertEquals("test@example.com", result.email());
        assertEquals("hashedPassword123", result.password());
        assertEquals("John", result.firstName());
        assertEquals("Doe", result.lastName());
        assertEquals("1990-01-01", result.dateOfBirth());
        assertEquals("Male", result.gender());
        assertEquals("San Francisco", result.location());
        assertNotNull(result.createdAt());
    }

    @Test
    void createFromApplication_usesGeneratedConnectId() {
        Application application = Application.builder()
                .applicationId("APP-789")
                .email("another@example.com")
                .hashedPassword("pass")
                .firstName("Jane")
                .lastName("Smith")
                .dateOfBirth("1995-05-15")
                .gender("Female")
                .location("New York")
                .status("pending")
                .createdAt(Instant.now())
                .build();

        when(connectIdGenerator.generateConnectId()).thenReturn(99999L);

        RegisteredUser result = approvedUserFactory.createFromApplication(application);

        assertEquals(99999L, result.connectId());
    }
}

