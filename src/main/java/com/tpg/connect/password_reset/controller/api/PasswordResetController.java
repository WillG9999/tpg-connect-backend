package com.tpg.connect.password_reset.controller.api;

import com.tpg.connect.password_reset.model.request.ChangePasswordRequest;
import com.tpg.connect.password_reset.model.request.ForgotPasswordRequest;
import com.tpg.connect.password_reset.model.request.ResetPasswordRequest;
import com.tpg.connect.password_reset.model.request.VerifyResetTokenRequest;
import com.tpg.connect.password_reset.model.response.PasswordResetResponse;
import com.tpg.connect.password_reset.model.response.VerifyResetTokenResponse;
import com.tpg.connect.password_reset.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PasswordResetController implements PasswordResetApi {

    private final PasswordResetService passwordResetService;

    @Override
    public ResponseEntity<PasswordResetResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        log.info("Forgot password request for: {}", request.email());
        PasswordResetResponse response = passwordResetService.forgotPassword(request.email());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<VerifyResetTokenResponse> verifyResetToken(
            @Valid @RequestBody VerifyResetTokenRequest request
    ) {
        log.info("Verify reset token request");
        VerifyResetTokenResponse response = passwordResetService.verifyResetToken(request.token());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<PasswordResetResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        log.info("Reset password request");
        PasswordResetResponse response = passwordResetService.resetPassword(request.token(), request.newPassword());
        if (!response.success())
            return ResponseEntity.badRequest().body(response);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<PasswordResetResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        log.info("Change password request for: {}", email);
        PasswordResetResponse response = passwordResetService.changePassword(
                email, request.currentPassword(), request.newPassword()
        );

        if (!response.success())
            return ResponseEntity.badRequest().body(response);
        return ResponseEntity.ok(response);
    }
}

