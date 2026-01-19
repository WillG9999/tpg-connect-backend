package com.tpg.connect.unit.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.tpg.connect.password_reset.exceptions.TokenSaveException;
import com.tpg.connect.password_reset.exceptions.TokenUpdateException;
import com.tpg.connect.password_reset.model.entity.PasswordResetToken;
import com.tpg.connect.password_reset.repository.PasswordResetTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PasswordResetTokenRepositoryTest {

    @Mock
    private Firestore firestore;

    @Mock
    private CollectionReference collectionReference;

    @Mock
    private DocumentReference documentReference;

    @Mock
    private ApiFuture<WriteResult> writeResultFuture;

    @Mock
    private ApiFuture<DocumentSnapshot> documentSnapshotFuture;

    @Mock
    private DocumentSnapshot documentSnapshot;

    private PasswordResetTokenRepository passwordResetTokenRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        passwordResetTokenRepository = new PasswordResetTokenRepository(firestore);
    }

    @Test
    void save_savesToken_whenSuccessful() throws Exception {
        PasswordResetToken token = PasswordResetToken.builder()
                .token("test-token-uuid")
                .email("user@example.com")
                .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .used(false)
                .build();

        when(firestore.collection("passwordResetTokens")).thenReturn(collectionReference);
        when(collectionReference.document("test-token-uuid")).thenReturn(documentReference);
        when(documentReference.set(any())).thenReturn(writeResultFuture);
        when(writeResultFuture.get()).thenReturn(mock(WriteResult.class));

        assertDoesNotThrow(() -> passwordResetTokenRepository.save(token));

        verify(documentReference).set(any());
    }

    @Test
    void save_throwsTokenSaveException_whenFirestoreFails() throws Exception {
        PasswordResetToken token = PasswordResetToken.builder()
                .token("test-token-uuid")
                .email("user@example.com")
                .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .used(false)
                .build();

        when(firestore.collection("passwordResetTokens")).thenReturn(collectionReference);
        when(collectionReference.document("test-token-uuid")).thenReturn(documentReference);
        when(documentReference.set(any())).thenReturn(writeResultFuture);
        when(writeResultFuture.get()).thenThrow(new RuntimeException("Firestore error"));

        assertThrows(TokenSaveException.class, () -> passwordResetTokenRepository.save(token));
    }

    @Test
    void findByToken_returnsToken_whenExists() throws Exception {
        String tokenId = "existing-token";
        Instant expiresAt = Instant.now().plus(1, ChronoUnit.HOURS);

        when(firestore.collection("passwordResetTokens")).thenReturn(collectionReference);
        when(collectionReference.document(tokenId)).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(documentSnapshotFuture);
        when(documentSnapshotFuture.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.getString("token")).thenReturn(tokenId);
        when(documentSnapshot.getString("email")).thenReturn("user@example.com");
        when(documentSnapshot.getString("expiresAt")).thenReturn(expiresAt.toString());
        when(documentSnapshot.getBoolean("used")).thenReturn(false);

        Optional<PasswordResetToken> result = passwordResetTokenRepository.findByToken(tokenId);

        assertTrue(result.isPresent());
        assertEquals(tokenId, result.get().token());
        assertEquals("user@example.com", result.get().email());
        assertFalse(result.get().used());
    }

    @Test
    void findByToken_returnsEmpty_whenNotExists() throws Exception {
        String tokenId = "nonexistent-token";

        when(firestore.collection("passwordResetTokens")).thenReturn(collectionReference);
        when(collectionReference.document(tokenId)).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(documentSnapshotFuture);
        when(documentSnapshotFuture.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(false);

        Optional<PasswordResetToken> result = passwordResetTokenRepository.findByToken(tokenId);

        assertFalse(result.isPresent());
    }

    @Test
    void findByToken_returnsEmpty_whenFirestoreFails() throws Exception {
        String tokenId = "error-token";

        when(firestore.collection("passwordResetTokens")).thenReturn(collectionReference);
        when(collectionReference.document(tokenId)).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(documentSnapshotFuture);
        when(documentSnapshotFuture.get()).thenThrow(new RuntimeException("Firestore error"));

        Optional<PasswordResetToken> result = passwordResetTokenRepository.findByToken(tokenId);

        assertFalse(result.isPresent());
    }

    @Test
    void markAsUsed_updatesToken_whenSuccessful() throws Exception {
        String tokenId = "token-to-mark";

        when(firestore.collection("passwordResetTokens")).thenReturn(collectionReference);
        when(collectionReference.document(tokenId)).thenReturn(documentReference);
        when(documentReference.update("used", true)).thenReturn(writeResultFuture);
        when(writeResultFuture.get()).thenReturn(mock(WriteResult.class));

        assertDoesNotThrow(() -> passwordResetTokenRepository.markAsUsed(tokenId));

        verify(documentReference).update("used", true);
    }

    @Test
    void markAsUsed_throwsTokenUpdateException_whenFirestoreFails() throws Exception {
        String tokenId = "token-to-mark";

        when(firestore.collection("passwordResetTokens")).thenReturn(collectionReference);
        when(collectionReference.document(tokenId)).thenReturn(documentReference);
        when(documentReference.update("used", true)).thenReturn(writeResultFuture);
        when(writeResultFuture.get()).thenThrow(new RuntimeException("Firestore error"));

        assertThrows(TokenUpdateException.class, () -> passwordResetTokenRepository.markAsUsed(tokenId));
    }
}

