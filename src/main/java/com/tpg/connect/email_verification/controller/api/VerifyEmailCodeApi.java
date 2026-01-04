package com.tpg.connect.email_verification.controller.api;

import com.tpg.connect.email_verification.model.request.VerifyEmailCodeRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Verify the User is correct",
                    required = true,
                    content = @Content(schema = @Schema(implementation = VerifyEmailCodeRequest.class))
            )
            @Valid @RequestBody VerifyEmailCodeRequest request
    );
}

