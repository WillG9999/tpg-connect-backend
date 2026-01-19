package com.tpg.connect.unit.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.tpg.connect.application.mapper.ApplicationMapper;
import com.tpg.connect.application.model.entity.Application;
import com.tpg.connect.application.repository.ApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ApplicationRepositoryTest {

    @Mock
    private Firestore firestore;

    @Mock
    private ApplicationMapper applicationMapper;

    @Mock
    private CollectionReference collectionReference;

    @Mock
    private DocumentReference documentReference;

    @Mock
    private ApiFuture<DocumentSnapshot> documentSnapshotFuture;

    @Mock
    private DocumentSnapshot documentSnapshot;

    private ApplicationRepository applicationRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        applicationRepository = new ApplicationRepository(firestore, applicationMapper);
    }

    @Test
    void findByEmail_returnsApplication_whenExists() throws Exception {
        String email = "test@example.com";
        Instant createdAt = Instant.now();

        Application expectedApplication = Application.builder()
                .applicationId("APP-123456")
                .email(email)
                .firstName("John")
                .lastName("Doe")
                .status("pending")
                .createdAt(createdAt)
                .build();

        when(firestore.collection("emailToAppIdLookup")).thenReturn(collectionReference);
        when(collectionReference.document(email)).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(documentSnapshotFuture);
        when(documentSnapshotFuture.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.getString("applicationId")).thenReturn("APP-123456");

        CollectionReference appCollection = mock(CollectionReference.class);
        DocumentReference appDocRef = mock(DocumentReference.class);
        ApiFuture<DocumentSnapshot> appDocFuture = mock(ApiFuture.class);
        DocumentSnapshot appDoc = mock(DocumentSnapshot.class);

        when(firestore.collection("applications")).thenReturn(appCollection);
        when(appCollection.document("APP-123456")).thenReturn(appDocRef);
        when(appDocRef.get()).thenReturn(appDocFuture);
        when(appDocFuture.get()).thenReturn(appDoc);
        when(appDoc.exists()).thenReturn(true);
        when(applicationMapper.documentToApplication(appDoc)).thenReturn(expectedApplication);

        Optional<Application> result = applicationRepository.findByEmail(email);

        assertTrue(result.isPresent());
        assertEquals("APP-123456", result.get().applicationId());
        assertEquals(email, result.get().email());
        assertEquals("pending", result.get().status());
    }

    @Test
    void findByEmail_returnsEmpty_whenEmailNotInLookup() throws Exception {
        String email = "notfound@example.com";

        when(firestore.collection("emailToAppIdLookup")).thenReturn(collectionReference);
        when(collectionReference.document(email)).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(documentSnapshotFuture);
        when(documentSnapshotFuture.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(false);

        Optional<Application> result = applicationRepository.findByEmail(email);

        assertFalse(result.isPresent());
    }

    @Test
    void findByEmail_returnsEmpty_whenApplicationNotFound() throws Exception {
        String email = "test@example.com";

        when(firestore.collection("emailToAppIdLookup")).thenReturn(collectionReference);
        when(collectionReference.document(email)).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(documentSnapshotFuture);
        when(documentSnapshotFuture.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.getString("applicationId")).thenReturn("APP-123456");

        CollectionReference appCollection = mock(CollectionReference.class);
        DocumentReference appDocRef = mock(DocumentReference.class);
        ApiFuture<DocumentSnapshot> appDocFuture = mock(ApiFuture.class);
        DocumentSnapshot appDoc = mock(DocumentSnapshot.class);

        when(firestore.collection("applications")).thenReturn(appCollection);
        when(appCollection.document("APP-123456")).thenReturn(appDocRef);
        when(appDocRef.get()).thenReturn(appDocFuture);
        when(appDocFuture.get()).thenReturn(appDoc);
        when(appDoc.exists()).thenReturn(false);

        Optional<Application> result = applicationRepository.findByEmail(email);

        assertFalse(result.isPresent());
    }

    @Test
    void findByEmail_returnsEmpty_whenFirestoreFails() throws Exception {
        String email = "test@example.com";

        when(firestore.collection("emailToAppIdLookup")).thenReturn(collectionReference);
        when(collectionReference.document(email)).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(documentSnapshotFuture);
        when(documentSnapshotFuture.get()).thenThrow(new RuntimeException("Firestore error"));

        Optional<Application> result = applicationRepository.findByEmail(email);

        assertFalse(result.isPresent());
    }
}

