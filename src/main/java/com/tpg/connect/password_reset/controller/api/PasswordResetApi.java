package com.tpg.connect.password_reset.controller.api;

import com.tpg.connect.password_reset.model.request.ChangePasswordRequest;
import com.tpg.connect.password_reset.model.request.ForgotPasswordRequest;
import com.tpg.connect.password_reset.model.request.ResetPasswordRequest;
import com.tpg.connect.password_reset.model.request.VerifyResetTokenRequest;
import com.tpg.connect.password_reset.model.response.PasswordResetResponse;
import com.tpg.connect.password_reset.model.response.VerifyResetTokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import static com.tpg.connect.common.constants.ConnectApiEndpointConstants.*;

public interface PasswordResetApi {

    @Operation(summary = "Request password reset", description = "Send password reset email to user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reset email sent if account exists")
    })
    @PostMapping(FORGOT_PASSWORD_ENDPOINT)
    ResponseEntity<PasswordResetResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request);

    @Operation(summary = "Verify reset token", description = "Check if password reset token is valid")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token verification result")
    })
    @PostMapping(VERIFY_RESET_TOKEN_ENDPOINT)
    ResponseEntity<VerifyResetTokenResponse> verifyResetToken(@Valid @RequestBody VerifyResetTokenRequest request);

    @Operation(summary = "Reset password", description = "Reset password using token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset successful"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token")
    })
    @PostMapping(RESET_PASSWORD_ENDPOINT)
    ResponseEntity<PasswordResetResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request);

    @Operation(summary = "Change password", description = "Change password for authenticated user", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Current password incorrect"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @PutMapping(CHANGE_PASSWORD_ENDPOINT)
    ResponseEntity<PasswordResetResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request);
}

