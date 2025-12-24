package com.tpg.connect.session_authentication.user_registration.repository;

import com.google.cloud.firestore.Firestore;
import com.tpg.connect.session_authentication.user_registration.model.entity.RegisteredUser;
import com.tpg.connect.session_authentication.user_registration.repository.api.RegisterUserRepositoryApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RegisterUserRepository implements RegisterUserRepositoryApi {

    private static final String COLLECTION_NAME = "RegisteredUsers";
    private final Firestore firestore;

    @Override
    public boolean saveUser(RegisteredUser user) {

        try {
            Map<String, Object> registeredUserData = getStringObjectMap(user);
            firestore.collection(COLLECTION_NAME)
                    .document(String.valueOf(user.connectId()))
                    .set(registeredUserData)
                    .get();

            log.info("User registered successfully with connectId:: {}", user.connectId());
            return true;
        } catch (Exception e) {
            log.error("Failed to register user:: connectId={}, {}",user.connectId(),e.getMessage());
            return false;
        }
    }

    private static Map<String, Object> getStringObjectMap(RegisteredUser user) {

        Map<String, Object> registeredUserData = new HashMap<>();
        registeredUserData.put("connectId", user.connectId());
        registeredUserData.put("email", user.email());
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
