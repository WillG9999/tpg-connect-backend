package com.tpg.connect.application.controller.api;

import com.tpg.connect.application.model.request.ApplicationStatusRequest;
import com.tpg.connect.application.model.request.ApplicationSubmissionRequest;
import com.tpg.connect.application.model.response.ApplicationStatusResponse;
import com.tpg.connect.application.model.response.ApplicationSubmissionResponse;
import com.tpg.connect.application.model.response.ApplicationSubmissionResult;
import com.tpg.connect.application.service.ApplicationService;
import com.tpg.connect.application.service.ApplicationStatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ApplicationController implements ApplicationApi, ApplicationStatusApi {

    private final ApplicationService applicationService;
    private final ApplicationStatusService applicationStatusService;

    @Override
    public ResponseEntity<ApplicationSubmissionResponse> submitApplication(
            @Valid @ModelAttribute ApplicationSubmissionRequest request
    ) {
        log.info("Application submission request received for: {}", request.email());

        ApplicationSubmissionResult result = applicationService.submitApplication(request);

        return ResponseEntity.ok(new ApplicationSubmissionResponse(result.applicationId(), result.message()));
    }

    @Override
    public ResponseEntity<ApplicationStatusResponse> getApplicationStatus(
            @Valid @RequestBody ApplicationStatusRequest request
    ) {
        log.info("Application status check for: {}", request.email());

        return applicationStatusService.getStatus(request.email())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

