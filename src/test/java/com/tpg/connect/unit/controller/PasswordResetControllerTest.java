package com.tpg.connect.unit.controller;

import com.tpg.connect.password_reset.controller.api.PasswordResetController;
import com.tpg.connect.password_reset.model.request.ChangePasswordRequest;
import com.tpg.connect.password_reset.model.request.ForgotPasswordRequest;
import com.tpg.connect.password_reset.model.request.ResetPasswordRequest;
import com.tpg.connect.password_reset.model.request.VerifyResetTokenRequest;
import com.tpg.connect.password_reset.model.response.PasswordResetResponse;
import com.tpg.connect.password_reset.model.response.VerifyResetTokenResponse;
import com.tpg.connect.password_reset.service.PasswordResetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PasswordResetControllerTest {

    @Mock
    private PasswordResetService passwordResetService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private PasswordResetController passwordResetController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        passwordResetController = new PasswordResetController(passwordResetService);
    }

    @Test
    void forgotPassword_returnsOk_always() {
        ForgotPasswordRequest request = new ForgotPasswordRequest("user@example.com");
        PasswordResetResponse serviceResponse = new PasswordResetResponse(
                true, "If the email exists, a reset link has been sent"
        );

        when(passwordResetService.forgotPassword("user@example.com")).thenReturn(serviceResponse);

        ResponseEntity<PasswordResetResponse> response = passwordResetController.forgotPassword(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().success());
    }

    @Test
    void verifyResetToken_returnsOk_withValidFlag() {
        VerifyResetTokenRequest request = new VerifyResetTokenRequest("valid-token");
        VerifyResetTokenResponse serviceResponse = new VerifyResetTokenResponse(true, "user@example.com");

        when(passwordResetService.verifyResetToken("valid-token")).thenReturn(serviceResponse);

        ResponseEntity<VerifyResetTokenResponse> response = passwordResetController.verifyResetToken(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().valid());
        assertEquals("user@example.com", response.getBody().email());
    }

    @Test
    void verifyResetToken_returnsOk_withInvalidFlag() {
        VerifyResetTokenRequest request = new VerifyResetTokenRequest("invalid-token");
        VerifyResetTokenResponse serviceResponse = new VerifyResetTokenResponse(false, null);

        when(passwordResetService.verifyResetToken("invalid-token")).thenReturn(serviceResponse);

        ResponseEntity<VerifyResetTokenResponse> response = passwordResetController.verifyResetToken(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().valid());
        assertNull(response.getBody().email());
    }

    @Test
    void resetPassword_returnsOk_whenSuccessful() {
        ResetPasswordRequest request = new ResetPasswordRequest("valid-token", "newPassword123");
        PasswordResetResponse serviceResponse = new PasswordResetResponse(
                true, "Password has been reset successfully"
        );

        when(passwordResetService.resetPassword("valid-token", "newPassword123"))
                .thenReturn(serviceResponse);

        ResponseEntity<PasswordResetResponse> response = passwordResetController.resetPassword(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().success());
    }

    @Test
    void resetPassword_returnsBadRequest_whenTokenInvalid() {
        ResetPasswordRequest request = new ResetPasswordRequest("invalid-token", "newPassword123");
        PasswordResetResponse serviceResponse = new PasswordResetResponse(
                false, "Invalid or expired reset token"
        );

        when(passwordResetService.resetPassword("invalid-token", "newPassword123"))
                .thenReturn(serviceResponse);

        ResponseEntity<PasswordResetResponse> response = passwordResetController.resetPassword(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().success());
    }

    @Test
    void changePassword_returnsOk_whenSuccessful() {
        ChangePasswordRequest request = new ChangePasswordRequest("currentPass", "newPass");
        PasswordResetResponse serviceResponse = new PasswordResetResponse(
                true, "Password changed successfully"
        );

        when(authentication.getName()).thenReturn("user@example.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(passwordResetService.changePassword("user@example.com", "currentPass", "newPass"))
                .thenReturn(serviceResponse);

        ResponseEntity<PasswordResetResponse> response = passwordResetController.changePassword(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().success());
    }

    @Test
    void changePassword_returnsBadRequest_whenCurrentPasswordWrong() {
        ChangePasswordRequest request = new ChangePasswordRequest("wrongPass", "newPass");
        PasswordResetResponse serviceResponse = new PasswordResetResponse(
                false, "Current password is incorrect"
        );

        when(authentication.getName()).thenReturn("user@example.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(passwordResetService.changePassword("user@example.com", "wrongPass", "newPass"))
                .thenReturn(serviceResponse);

        ResponseEntity<PasswordResetResponse> response = passwordResetController.changePassword(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().success());
    }
}

