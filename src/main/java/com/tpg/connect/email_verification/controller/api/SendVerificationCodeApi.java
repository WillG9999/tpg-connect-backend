 package com.tpg.connect.email_verification.controller.api;

 import com.tpg.connect.email_verification.model.request.SendVerificationCodeRequest;
 import io.swagger.v3.oas.annotations.Operation;
 import io.swagger.v3.oas.annotations.media.Content;
 import io.swagger.v3.oas.annotations.media.Schema;
 import io.swagger.v3.oas.annotations.responses.ApiResponse;
 import io.swagger.v3.oas.annotations.responses.ApiResponses;
 import jakarta.validation.Valid;
 import org.springframework.http.ResponseEntity;
 import org.springframework.web.bind.annotation.PostMapping;
 import org.springframework.web.bind.annotation.RequestBody;

 import static com.tpg.connect.common.constants.ConnectApiEndpointConstants.SEND_VERIFICATION_ENDPOINT;

 public interface SendVerificationCodeApi {

    @Operation(
            summary = "Send email verification code",
            description = "Sends a verification code to the specified email address",
            tags = {"Email Verification"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Verification code sent successfully",
                    content = @Content
            )
    })
    @PostMapping(SEND_VERIFICATION_ENDPOINT)
    ResponseEntity<Void> sendVerificationCode(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Request Containing Verification Codes",
                    required = true,
                    content = @Content(schema = @Schema(implementation = SendVerificationCodeRequest.class))
            )
            @Valid @RequestBody SendVerificationCodeRequest request
    );
}
