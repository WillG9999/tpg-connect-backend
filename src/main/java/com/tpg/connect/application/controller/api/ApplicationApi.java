package com.tpg.connect.application.controller.api;

import com.tpg.connect.application.model.request.ApplicationSubmissionRequest;
import com.tpg.connect.application.model.response.ApplicationSubmissionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import static com.tpg.connect.common.constants.ConnectApiEndpointConstants.APPLICATION_SUBMIT_ENDPOINT;

public interface ApplicationApi {

    @Operation(
            summary = "Submit application",
            description = "Submit a new user application with profile information and photos",
            tags = {"Application"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Application submitted successfully",
                    content = @Content(schema = @Schema(implementation = ApplicationSubmissionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Email already registered"
            )
    })
    @PostMapping(value = APPLICATION_SUBMIT_ENDPOINT, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<ApplicationSubmissionResponse> submitApplication(
            @Valid @ModelAttribute ApplicationSubmissionRequest request
    );
}

