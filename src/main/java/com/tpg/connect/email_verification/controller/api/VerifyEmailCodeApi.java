package com.tpg.connect.email_verification.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static com.tpg.connect.common.constants.ConnectApiEndpointConstants.VERIFY_EMAIL_CODE_ENDPOINT;

public interface VerifyEmailCodeApi {

    @Operation(
            summary = "Verify email code",
            description = "Verifies the code sent to the specified email address",
            tags = {"Email Verification"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Verification code is valid",
                    content = @Content
            )
    })

    @PostMapping(VERIFY_EMAIL_CODE_ENDPOINT)
    ResponseEntity<Void> verifyEmailCode(
            @Parameter(description = "User email", required = true)
            @RequestParam("email") String email,
            @Parameter(description = "Verification code", required = true)
            @RequestParam("verification") String verificationCode
    );
}
