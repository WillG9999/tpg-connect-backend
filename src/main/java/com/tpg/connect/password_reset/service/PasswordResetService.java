package com.tpg.connect.password_reset.service;

import com.tpg.connect.common.services.PasswordService;
import com.tpg.connect.external.email.client.EmailClient;
import com.tpg.connect.password_reset.model.entity.PasswordResetToken;
import com.tpg.connect.password_reset.model.response.PasswordResetResponse;
import com.tpg.connect.password_reset.model.response.VerifyResetTokenResponse;
import com.tpg.connect.password_reset.repository.PasswordResetTokenRepository;
import com.tpg.connect.user_registration.repository.api.RegisterUserRepositoryApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final Duration TOKEN_VALIDITY = Duration.ofHours(1);

    private final PasswordResetTokenRepository tokenRepository;
    private final RegisterUserRepositoryApi userRepository;
    private final PasswordService passwordService;
    private final EmailClient emailClient;

    public PasswordResetResponse forgotPassword(String email) {
        log.info("Processing forgot password request for: {}", email);

        String normalizedEmail = email.toLowerCase().trim();

        if (userRepository.findByEmail(normalizedEmail).isEmpty()) {
            log.warn("Forgot password requested for non-existent email: {}", normalizedEmail);
            return new PasswordResetResponse(true, "If the email exists, a reset link has been sent");
        }

        tokenRepository.deleteByEmail(normalizedEmail);

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .email(normalizedEmail)
                .expiresAt(Instant.now().plus(TOKEN_VALIDITY))
                .used(false)
                .build();

        tokenRepository.save(resetToken);

        boolean emailSent = emailClient.sendPasswordResetEmail(normalizedEmail, token);
        if (!emailSent)
            log.error("Failed to send password reset email to: {}", normalizedEmail);

        log.info("Password reset token generated for: {}", normalizedEmail);
        return new PasswordResetResponse(true, "If the email exists, a reset link has been sent");
    }

    public VerifyResetTokenResponse verifyResetToken(String token) {
        log.info("Verifying password reset token");

        Optional<PasswordResetToken> resetToken = tokenRepository.findByToken(token);

        if (resetToken.isEmpty()) {
            log.warn("Password reset token not found");
            return new VerifyResetTokenResponse(false, null);
        }

        PasswordResetToken tokenEntity = resetToken.get();

        if (tokenEntity.used()) {
            log.warn("Password reset token already used");
            return new VerifyResetTokenResponse(false, null);
        }

        if (Instant.now().isAfter(tokenEntity.expiresAt())) {
            log.warn("Password reset token expired");
            return new VerifyResetTokenResponse(false, null);
        }

        log.info("Password reset token is valid for email: {}", tokenEntity.email());
        return new VerifyResetTokenResponse(true, tokenEntity.email());
    }

    public PasswordResetResponse resetPassword(String token, String newPassword) {
        log.info("Processing password reset");

        VerifyResetTokenResponse verification = verifyResetToken(token);
        if (!verification.valid()) {
            log.warn("Invalid or expired reset token");
            return new PasswordResetResponse(false, "Invalid or expired reset token");
        }

        String email = verification.email();
        String hashedPassword = passwordService.hashPassword(newPassword);

        boolean updated = userRepository.updatePassword(email, hashedPassword);
        if (!updated) {
            log.error("Failed to update password for: {}", email);
            return new PasswordResetResponse(false, "Failed to update password");
        }

        tokenRepository.markAsUsed(token);

        log.info("Password reset successful for: {}", email);
        return new PasswordResetResponse(true, "Password has been reset successfully");
    }

    public PasswordResetResponse changePassword(String email, String currentPassword, String newPassword) {
        log.info("Processing password change for: {}", email);

        var userOpt = userRepository.findByEmail(email.toLowerCase().trim());
        if (userOpt.isEmpty()) {
            log.warn("User not found for password change: {}", email);
            return new PasswordResetResponse(false, "User not found");
        }

        var user = userOpt.get();
        String decodedCurrentPassword = passwordService.decodeBase64Password(currentPassword);

        if (!passwordService.matches(decodedCurrentPassword, user.password())) {
            log.warn("Current password mismatch for: {}", email);
            return new PasswordResetResponse(false, "Current password is incorrect");
        }

        String hashedNewPassword = passwordService.hashPassword(newPassword);
        boolean updated = userRepository.updatePassword(email, hashedNewPassword);

        if (!updated) {
            log.error("Failed to update password for: {}", email);
            return new PasswordResetResponse(false, "Failed to update password");
        }

        log.info("Password changed successfully for: {}", email);
        return new PasswordResetResponse(true, "Password changed successfully");
    }
}

