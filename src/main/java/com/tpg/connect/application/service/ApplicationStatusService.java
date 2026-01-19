package com.tpg.connect.application.service;

import com.tpg.connect.application.model.entity.Application;
import com.tpg.connect.application.model.response.ApplicationStatusResponse;
import com.tpg.connect.application.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationStatusService {

    private final ApplicationRepository applicationRepository;

    public Optional<ApplicationStatusResponse> getStatus(String email) {
        log.info("Checking application status for email: {}", email);

        Optional<Application> application = applicationRepository.findByEmail(email);

        return application.map(app -> new ApplicationStatusResponse(
                app.applicationId(),
                app.status(),
                app.createdAt().toString()
        ));
    }
}

