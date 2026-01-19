package com.tpg.connect.password_reset.repository;

import com.google.cloud.firestore.Firestore;
import com.tpg.connect.password_reset.exceptions.TokenSaveException;
import com.tpg.connect.password_reset.exceptions.TokenUpdateException;
import com.tpg.connect.password_reset.model.entity.PasswordResetToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PasswordResetTokenRepository {

    private static final String COLLECTION = "passwordResetTokens";

    private final Firestore firestore;

    public void save(PasswordResetToken token) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("token", token.token());
            data.put("email", token.email().toLowerCase().trim());
            data.put("expiresAt", token.expiresAt().toString());
            data.put("used", token.used());

            firestore.collection(COLLECTION).document(token.token()).set(data).get();
            log.info("Password reset token saved for email: {}", token.email());
        } catch (Exception e) {
            log.error("Failed to save password reset token", e);
            throw new TokenSaveException(token.email(), e);
        }
    }

    public Optional<PasswordResetToken> findByToken(String token) {
        try {
            var doc = firestore.collection(COLLECTION).document(token).get().get();
            if (!doc.exists()) {
                return Optional.empty();
            }

            return Optional.of(PasswordResetToken.builder()
                    .token(doc.getString("token"))
                    .email(doc.getString("email"))
                    .expiresAt(Instant.parse(doc.getString("expiresAt")))
                    .used(Boolean.TRUE.equals(doc.getBoolean("used")))
                    .build());
        } catch (Exception e) {
            log.error("Failed to find password reset token", e);
            return Optional.empty();
        }
    }

    public void markAsUsed(String token) {
        try {
            firestore.collection(COLLECTION).document(token).update("used", true).get();
            log.info("Password reset token marked as used: {}", token);
        } catch (Exception e) {
            log.error("Failed to mark token as used", e);
            throw new TokenUpdateException(token, e);
        }
    }

    public void deleteByEmail(String email) {
        try {
            var query = firestore.collection(COLLECTION)
                    .whereEqualTo("email", email.toLowerCase().trim())
                    .get().get();

            for (var doc : query.getDocuments()) {
                doc.getReference().delete().get();
            }
            log.info("Deleted password reset tokens for email: {}", email);
        } catch (Exception e) {
            log.error("Failed to delete tokens for email: {}", email, e);
        }
    }
}

