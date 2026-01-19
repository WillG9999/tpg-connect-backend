package com.tpg.connect.user_registration.repository;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.tpg.connect.user_registration.exceptions.UserRegistrationException;
import com.tpg.connect.user_registration.mapper.RegisteredUserMapper;
import com.tpg.connect.user_registration.model.entity.RegisteredUser;
import com.tpg.connect.user_registration.repository.api.RegisterUserRepositoryApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

import static com.tpg.connect.common.constants.RepositoryNamesConstants.EMAILTOCONNECTIDLOOKUP;
import static com.tpg.connect.common.constants.RepositoryNamesConstants.REGISTERED_USERS;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RegisterUserRepository implements RegisterUserRepositoryApi {

    private final Firestore firestore;
    private final RegisteredUserMapper userMapper;

    @Override
    public Optional<RegisteredUser> findByEmail(String email) {
        String normalizedEmail = email.toLowerCase().trim();
        log.debug("Looking up user by email: {}", normalizedEmail);

        try {
            DocumentSnapshot emailLookupDoc = firestore.collection(EMAILTOCONNECTIDLOOKUP)
                    .document(normalizedEmail)
                    .get()
                    .get();

            if (!emailLookupDoc.exists()) {
                log.debug("No user found with email: {}", normalizedEmail);
                return Optional.empty();
            }

            String connectId = emailLookupDoc.getString("connectId");
            if (connectId == null) {
                log.warn("Email lookup document exists but has no connectId for: {}", normalizedEmail);
                return Optional.empty();
            }

            DocumentSnapshot userDoc = firestore.collection(REGISTERED_USERS)
                    .document(connectId)
                    .get()
                    .get();

            if (!userDoc.exists()) {
                log.warn("User document not found for connectId: {}", connectId);
                return Optional.empty();
            }

            RegisteredUser user = userMapper.documentToUser(userDoc, Long.parseLong(connectId));
            log.debug("User found with connectId: {}", connectId);
            return Optional.of(user);

        } catch (Exception e) {
            log.error("Error looking up user by email: {}", normalizedEmail, e);
            return Optional.empty();
        }
    }

    @Override
    public boolean saveUser(RegisteredUser user) {
        return saveUserWithApplicationId(user, null);
    }

    public boolean saveUserWithApplicationId(RegisteredUser user, String applicationId) {
        String email = user.email().toLowerCase().trim();
        Map<String, Object> registeredUserData = userMapper.userToDocument(user, applicationId);

        try {
            firestore.runTransaction(transaction -> {
                var userDoc = firestore.collection(REGISTERED_USERS)
                        .document(String.valueOf(user.connectId()));
                var emailLookupDoc = firestore.collection(EMAILTOCONNECTIDLOOKUP)
                        .document(email);
                if (transaction.get(emailLookupDoc).get().exists())
                    throw new UserRegistrationException("User attempting to register already exists " + email);

                transaction.create(userDoc, registeredUserData);

                Map<String, Object> lookupData = userMapper.createLookupData(user.connectId(), email, applicationId);
                transaction.create(emailLookupDoc, lookupData);
                return null;
            }).get();
            log.info("User registered successfully with connectId: {}, applicationId: {}", user.connectId(), applicationId);
            return true;
        } catch (Exception e) {
            log.error("Failed to register user possible duplicate :: connectId={}, {}", user.connectId(), e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updatePassword(String email, String hashedPassword) {
        String normalizedEmail = email.toLowerCase().trim();
        log.debug("Updating password for email: {}", normalizedEmail);

        try {
            DocumentSnapshot emailLookupDoc = firestore.collection(EMAILTOCONNECTIDLOOKUP)
                    .document(normalizedEmail)
                    .get()
                    .get();

            if (!emailLookupDoc.exists()) {
                log.warn("No user found for password update: {}", normalizedEmail);
                return false;
            }

            String connectId = emailLookupDoc.getString("connectId");
            if (connectId == null) {
                log.warn("Email lookup exists but no connectId for: {}", normalizedEmail);
                return false;
            }

            firestore.collection(REGISTERED_USERS)
                    .document(connectId)
                    .update("password", hashedPassword)
                    .get();

            log.info("Password updated successfully for connectId: {}", connectId);
            return true;

        } catch (Exception e) {
            log.error("Error updating password for email: {}", normalizedEmail, e);
            return false;
        }
    }
}
