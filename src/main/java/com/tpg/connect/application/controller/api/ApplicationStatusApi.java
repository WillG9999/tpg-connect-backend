package com.tpg.connect.application.controller.api;

import com.tpg.connect.application.model.request.ApplicationStatusRequest;
import com.tpg.connect.application.model.response.ApplicationStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import static com.tpg.connect.common.constants.ConnectApiEndpointConstants.APPLICATION_STATUS_ENDPOINT;

public interface ApplicationStatusApi {

    @Operation(summary = "Check application status", description = "Check the status of a submitted application by email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application status retrieved"),
            @ApiResponse(responseCode = "404", description = "Application not found")
    })
    @PostMapping(APPLICATION_STATUS_ENDPOINT)
    ResponseEntity<ApplicationStatusResponse> getApplicationStatus(@Valid @RequestBody ApplicationStatusRequest request);
}

