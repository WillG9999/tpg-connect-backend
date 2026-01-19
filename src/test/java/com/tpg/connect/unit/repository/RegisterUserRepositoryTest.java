package com.tpg.connect.unit.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.tpg.connect.common.constants.RepositoryNamesConstants;
import com.tpg.connect.user_registration.mapper.RegisteredUserMapper;
import com.tpg.connect.user_registration.model.entity.RegisteredUser;
import com.tpg.connect.user_registration.repository.RegisterUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class RegisterUserRepositoryTest {

    private Firestore firestore;
    private RegisteredUserMapper userMapper;
    private RegisterUserRepository underTest;

    @BeforeEach
    void setUp() {
        firestore = mock(Firestore.class);
        userMapper = mock(RegisteredUserMapper.class);
        underTest = new RegisterUserRepository(firestore, userMapper);
    }

    @Test
    void saveUser_writesToBothCollections() throws Exception {
        RegisteredUser user = buildUser("test@example.com", 1L);
        Map<String, Object> userDocData = new HashMap<>();
        userDocData.put("email", "test@example.com");
        Map<String, Object> lookupData = new HashMap<>();
        lookupData.put("connectId", "1");

        when(userMapper.userToDocument(user, null)).thenReturn(userDocData);
        when(userMapper.createLookupData(1L, "test@example.com", null)).thenReturn(lookupData);

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
            function.updateCallback(transaction);
            ApiFuture<Void> apiFuture = mock(ApiFuture.class);
            when(apiFuture.get()).thenReturn(null);
            return apiFuture;
        });

        boolean result = underTest.saveUser(user);

        assertTrue(result);
        verify(transaction).create(eq(userDoc), eq(userDocData));
        verify(transaction).create(eq(emailDoc), eq(lookupData));
    }

    @Test
    void saveUser_whenExceptionThrown_returnsFalse() {
        RegisteredUser user = buildUser("fail@example.com", 2L);
        when(userMapper.userToDocument(any(), any())).thenReturn(new HashMap<>());
        when(firestore.runTransaction(any())).thenThrow(new RuntimeException("Simulated failure"));

        boolean result = underTest.saveUser(user);

        assertFalse(result);
    }

    @Test
    void saveUserWithApplicationId_writesToBothCollectionsWithApplicationId() throws Exception {
        RegisteredUser user = buildUser("test@example.com", 1L);
        String applicationId = "APP-123456";

        Map<String, Object> userDocData = new HashMap<>();
        userDocData.put("email", "test@example.com");
        userDocData.put("applicationId", applicationId);
        Map<String, Object> lookupData = new HashMap<>();
        lookupData.put("connectId", "1");
        lookupData.put("applicationId", applicationId);

        when(userMapper.userToDocument(user, applicationId)).thenReturn(userDocData);
        when(userMapper.createLookupData(1L, "test@example.com", applicationId)).thenReturn(lookupData);

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
            Transaction.Function<Void> function = invocation.getArgument(0);
            function.updateCallback(transaction);
            ApiFuture<Void> apiFuture = mock(ApiFuture.class);
            when(apiFuture.get()).thenReturn(null);
            return apiFuture;
        });

        boolean result = underTest.saveUserWithApplicationId(user, applicationId);

        assertTrue(result);
        verify(transaction).create(eq(userDoc), eq(userDocData));
        verify(transaction).create(eq(emailDoc), eq(lookupData));
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
                "USER",
                Instant.now()
        );
    }
}
