package com.tpg.connect.session_authentication.user_registration.controller.api;

import com.tpg.connect.session_authentication.user_registration.model.request.UserRegistrationRequest;
import com.tpg.connect.session_authentication.user_registration.model.response.UserRegistrationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;


public interface RegisterApi {

    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account and returns a JWT token",
            tags = {"Authentication"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User registered successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserRegistrationResponse.class)
                    )
            )
    })

    @PostMapping("/v1/auth/register")
    ResponseEntity<UserRegistrationResponse> registerUser(
            @RequestBody(
                    description = "User registration details",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserRegistrationRequest.class))
            )
            UserRegistrationRequest request
    );
}
