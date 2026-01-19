package com.tpg.connect.unit.service;

import com.tpg.connect.common.services.PasswordService;
import com.tpg.connect.external.email.client.EmailClient;
import com.tpg.connect.password_reset.model.entity.PasswordResetToken;
import com.tpg.connect.password_reset.model.response.PasswordResetResponse;
import com.tpg.connect.password_reset.model.response.VerifyResetTokenResponse;
import com.tpg.connect.password_reset.repository.PasswordResetTokenRepository;
import com.tpg.connect.password_reset.service.PasswordResetService;
import com.tpg.connect.user_registration.model.entity.RegisteredUser;
import com.tpg.connect.user_registration.repository.api.RegisterUserRepositoryApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PasswordResetServiceTest {

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private RegisterUserRepositoryApi userRepository;

    @Mock
    private PasswordService passwordService;

    @Mock
    private EmailClient emailClient;

    private PasswordResetService passwordResetService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        passwordResetService = new PasswordResetService(
                tokenRepository,
                userRepository,
                passwordService,
                emailClient
        );
    }

    @Test
    void forgotPassword_returnsSuccess_whenUserExists() {
        String email = "user@example.com";
        RegisteredUser user = RegisteredUser.builder()
                .connectId(123L)
                .email(email)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(emailClient.sendPasswordResetEmail(eq(email), anyString())).thenReturn(true);

        PasswordResetResponse response = passwordResetService.forgotPassword(email);

        assertTrue(response.success());
        assertEquals("If the email exists, a reset link has been sent", response.message());
        verify(tokenRepository).deleteByEmail(email);
        verify(tokenRepository).save(any(PasswordResetToken.class));
        verify(emailClient).sendPasswordResetEmail(eq(email), anyString());
    }

    @Test
    void forgotPassword_returnsSuccess_whenUserNotExists() {
        String email = "notfound@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        PasswordResetResponse response = passwordResetService.forgotPassword(email);

        assertTrue(response.success());
        assertEquals("If the email exists, a reset link has been sent", response.message());
        verify(tokenRepository, never()).save(any());
        verify(emailClient, never()).sendPasswordResetEmail(anyString(), anyString());
    }

    @Test
    void forgotPassword_deletesExistingTokens_beforeCreatingNew() {
        String email = "user@example.com";
        RegisteredUser user = RegisteredUser.builder().connectId(123L).email(email).build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(emailClient.sendPasswordResetEmail(anyString(), anyString())).thenReturn(true);

        passwordResetService.forgotPassword(email);

        verify(tokenRepository).deleteByEmail(email);
        verify(tokenRepository).save(any(PasswordResetToken.class));
    }

    @Test
    void verifyResetToken_returnsValid_whenTokenExistsAndNotExpired() {
        String token = "valid-token-uuid";
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .email("user@example.com")
                .expiresAt(Instant.now().plus(30, ChronoUnit.MINUTES))
                .used(false)
                .build();

        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));

        VerifyResetTokenResponse response = passwordResetService.verifyResetToken(token);

        assertTrue(response.valid());
        assertEquals("user@example.com", response.email());
    }

    @Test
    void verifyResetToken_returnsInvalid_whenTokenNotFound() {
        when(tokenRepository.findByToken("nonexistent")).thenReturn(Optional.empty());

        VerifyResetTokenResponse response = passwordResetService.verifyResetToken("nonexistent");

        assertFalse(response.valid());
        assertNull(response.email());
    }

    @Test
    void verifyResetToken_returnsInvalid_whenTokenExpired() {
        String token = "expired-token";
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .email("user@example.com")
                .expiresAt(Instant.now().minus(1, ChronoUnit.HOURS))
                .used(false)
                .build();

        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));

        VerifyResetTokenResponse response = passwordResetService.verifyResetToken(token);

        assertFalse(response.valid());
        assertNull(response.email());
    }

    @Test
    void verifyResetToken_returnsInvalid_whenTokenAlreadyUsed() {
        String token = "used-token";
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .email("user@example.com")
                .expiresAt(Instant.now().plus(30, ChronoUnit.MINUTES))
                .used(true)
                .build();

        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));

        VerifyResetTokenResponse response = passwordResetService.verifyResetToken(token);

        assertFalse(response.valid());
        assertNull(response.email());
    }

    @Test
    void resetPassword_returnsSuccess_whenTokenValidAndPasswordUpdated() {
        String token = "valid-token";
        String newPassword = "newPassword123";
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .email("user@example.com")
                .expiresAt(Instant.now().plus(30, ChronoUnit.MINUTES))
                .used(false)
                .build();

        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));
        when(passwordService.hashPassword(newPassword)).thenReturn("hashedNewPassword");
        when(userRepository.updatePassword("user@example.com", "hashedNewPassword")).thenReturn(true);

        PasswordResetResponse response = passwordResetService.resetPassword(token, newPassword);

        assertTrue(response.success());
        assertEquals("Password has been reset successfully", response.message());
        verify(tokenRepository).markAsUsed(token);
    }

    @Test
    void resetPassword_returnsFailure_whenTokenInvalid() {
        when(tokenRepository.findByToken("invalid")).thenReturn(Optional.empty());

        PasswordResetResponse response = passwordResetService.resetPassword("invalid", "newPass");

        assertFalse(response.success());
        assertEquals("Invalid or expired reset token", response.message());
    }

    @Test
    void resetPassword_returnsFailure_whenPasswordUpdateFails() {
        String token = "valid-token";
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .email("user@example.com")
                .expiresAt(Instant.now().plus(30, ChronoUnit.MINUTES))
                .used(false)
                .build();

        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));
        when(passwordService.hashPassword(anyString())).thenReturn("hashed");
        when(userRepository.updatePassword(anyString(), anyString())).thenReturn(false);

        PasswordResetResponse response = passwordResetService.resetPassword(token, "newPass");

        assertFalse(response.success());
        assertEquals("Failed to update password", response.message());
    }

    @Test
    void changePassword_returnsSuccess_whenCurrentPasswordMatches() {
        String email = "user@example.com";
        String currentPassword = "Y3VycmVudFBhc3M=";
        String newPassword = "newPassword123";

        RegisteredUser user = RegisteredUser.builder()
                .connectId(123L)
                .email(email)
                .password("hashedCurrentPassword")
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordService.decodeBase64Password(currentPassword)).thenReturn("currentPass");
        when(passwordService.matches("currentPass", "hashedCurrentPassword")).thenReturn(true);
        when(passwordService.hashPassword(newPassword)).thenReturn("hashedNewPassword");
        when(userRepository.updatePassword(email, "hashedNewPassword")).thenReturn(true);

        PasswordResetResponse response = passwordResetService.changePassword(email, currentPassword, newPassword);

        assertTrue(response.success());
        assertEquals("Password changed successfully", response.message());
    }

    @Test
    void changePassword_returnsFailure_whenCurrentPasswordIncorrect() {
        String email = "user@example.com";
        String currentPassword = "d3JvbmdQYXNz";
        String newPassword = "newPassword123";

        RegisteredUser user = RegisteredUser.builder()
                .connectId(123L)
                .email(email)
                .password("hashedCurrentPassword")
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordService.decodeBase64Password(currentPassword)).thenReturn("wrongPass");
        when(passwordService.matches("wrongPass", "hashedCurrentPassword")).thenReturn(false);

        PasswordResetResponse response = passwordResetService.changePassword(email, currentPassword, newPassword);

        assertFalse(response.success());
        assertEquals("Current password is incorrect", response.message());
    }

    @Test
    void changePassword_returnsFailure_whenUserNotFound() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        PasswordResetResponse response = passwordResetService.changePassword(
                "notfound@example.com", "current", "new"
        );

        assertFalse(response.success());
        assertEquals("User not found", response.message());
    }
}

