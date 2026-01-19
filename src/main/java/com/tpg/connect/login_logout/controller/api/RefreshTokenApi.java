package com.tpg.connect.login_logout.controller.api;

import com.tpg.connect.login_logout.model.request.RefreshTokenRequest;
import com.tpg.connect.login_logout.model.response.RefreshTokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import static com.tpg.connect.common.constants.ConnectApiEndpointConstants.REFRESH_TOKEN_ENDPOINT;

public interface RefreshTokenApi {

    @Operation(
            summary = "Refresh access token",
            description = "Uses a valid refresh token to obtain a new access token and refresh token pair",
            tags = {"Authentication"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token refreshed successfully",
                    content = @Content(schema = @Schema(implementation = RefreshTokenResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid or expired refresh token",
                    content = @Content
            )
    })
    @PostMapping(REFRESH_TOKEN_ENDPOINT)
    ResponseEntity<RefreshTokenResponse> refreshToken(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Refresh token",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RefreshTokenRequest.class))
            )
            @Valid @RequestBody RefreshTokenRequest request
    );
}

