package com.tpg.connect.login_logout.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

import static com.tpg.connect.common.constants.ConnectApiEndpointConstants.LOGOUT_ENDPOINT;

public interface LogoutApi {

    @Operation(
            summary = "User logout",
            description = "Logs out the current user and invalidates the session",
            tags = {"Authentication"},
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Logout successful"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Not authenticated"
            )
    })
    @PostMapping(LOGOUT_ENDPOINT)
    ResponseEntity<Void> logout();
}

