 package com.tpg.connect.unit.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.tpg.connect.common.constants.RepositoryNamesConstants;
import com.tpg.connect.user_registration.model.entity.RegisteredUser;
import com.tpg.connect.user_registration.repository.RegisterUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class RegisterUserRepositoryTest {

    private Firestore firestore;
    private RegisterUserRepository underTest;

    @BeforeEach
    void setUp() {
        firestore = mock(Firestore.class);
        underTest = new RegisterUserRepository(firestore);
    }

    @Test
    void saveUser_writesToBothCollections() throws Exception {

        RegisteredUser user = buildUser("test@example.com", 1L);

        Transaction transaction = mock(Transaction.class);
        DocumentReference userDoc = mock(DocumentReference.class);
        DocumentReference emailDoc = mock(DocumentReference.class);
        CollectionReference registeredUsers = mock(CollectionReference.class);
        CollectionReference emailLookup = mock(CollectionReference.class);

        when(firestore.collection(RepositoryNamesConstants.REGISTERED_USERS)).thenReturn(registeredUsers);
        when(firestore.collection(RepositoryNamesConstants.EMAILTOCONNECTIDLOOKUP)).thenReturn(emailLookup);
        when(registeredUsers.document(anyString())).thenReturn(userDoc);
        when(emailLookup.document(anyString())).thenReturn(emailDoc);

        ApiFuture<DocumentSnapshot> docSnapFuture = mock(ApiFuture.class);
        DocumentSnapshot emailDocSnap = mock(DocumentSnapshot.class);
        when(emailDocSnap.exists()).thenReturn(false);
        when(docSnapFuture.get()).thenReturn(emailDocSnap);
        when(transaction.get(emailDoc)).thenReturn(docSnapFuture);

        when(firestore.runTransaction(any())).thenAnswer(invocation -> {
            Transaction.Function<Boolean> function = invocation.getArgument(0);
            Boolean result = function.updateCallback(transaction);
            @SuppressWarnings("unchecked")
            ApiFuture<Boolean> apiFuture = mock(ApiFuture.class);
            when(apiFuture.get()).thenReturn(result);
            return apiFuture;
        });

        underTest.saveUser(user);

        verify(transaction).create(eq(userDoc), any(Map.class));
        verify(transaction).create(eq(emailDoc), any(Map.class));
    }

    @Test
    void saveUser_whenExceptionThrown_returnsFalse() throws Exception {

        RegisteredUser user = buildUser("fail@example.com", 2L);
        when(firestore.runTransaction(any())).thenThrow(new RuntimeException("Simulated failure"));
        boolean result = underTest.saveUser(user);

        assertFalse(result);
    }


    private RegisteredUser buildUser(String email, Long connectId) {
        return new RegisteredUser(
                connectId,
                email,
                "password",
                "First",
                "Last",
                "1990-01-01",
                "male",
                "location",
                Instant.now()
        );
    }
}
