package com.tpg.connect.user_registration.repository;

import com.google.cloud.firestore.Firestore;
import com.tpg.connect.user_registration.exceptions.UserRegistrationException;
import com.tpg.connect.user_registration.model.entity.RegisteredUser;
import com.tpg.connect.user_registration.repository.api.RegisterUserRepositoryApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

import static com.tpg.connect.common.constants.RepositoryNamesConstants.EMAILTOCONNECTIDLOOKUP;
import static com.tpg.connect.common.constants.RepositoryNamesConstants.REGISTERED_USERS;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RegisterUserRepository implements RegisterUserRepositoryApi {

    private final Firestore firestore;

    @Override
    public boolean saveUser(RegisteredUser user) {

        String email = user.email().toLowerCase().trim();
        Map<String, Object> registeredUserData = getStringObjectMap(user);

        try {
            firestore.runTransaction(transaction -> {
                var userDoc = firestore.collection(REGISTERED_USERS)
                        .document(String.valueOf(user.connectId()));
                var emailLookupDoc = firestore.collection(EMAILTOCONNECTIDLOOKUP)
                        .document(email);
                if (transaction.get(emailLookupDoc).get().exists())
                    throw new UserRegistrationException("User attempting to register already exists " + email);

                transaction.create(userDoc, registeredUserData);
                transaction.create(emailLookupDoc, Map.of("connectId", user.connectId()));
                return null;
            }).get();
            log.info("User registered successfully with connectId:: {}", user.connectId());
            return true;
        } catch (Exception e) {
            log.error("Failed to register user possible duplicate :: connectId={}, {}", user.connectId(), e.getMessage());
            return false;
        }
    }


    private static Map<String, Object> getStringObjectMap(RegisteredUser user) {

        Map<String, Object> registeredUserData = new HashMap<>();

        registeredUserData.put("connectId", String.valueOf(user.connectId()));
        registeredUserData.put("email", user.email().toLowerCase());
        registeredUserData.put("password", user.password());
        registeredUserData.put("firstName", user.firstName());
        registeredUserData.put("lastName", user.lastName());
        registeredUserData.put("dateOfBirth", user.dateOfBirth());
        registeredUserData.put("gender", user.gender());
        registeredUserData.put("location", user.location());
        registeredUserData.put("createdAt", user.createdAt().toString());

        return registeredUserData;
    }
}
