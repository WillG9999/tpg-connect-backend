package com.tpg.connect.admin.factory;

import com.tpg.connect.application.model.entity.Application;
import com.tpg.connect.user_registration.components.ConnectIdGenerator;
import com.tpg.connect.user_registration.model.entity.RegisteredUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class ApprovedUserFactory {

    private final ConnectIdGenerator connectIdGenerator;

    public RegisteredUser createFromApplication(Application application) {
        return RegisteredUser.builder()
                .connectId(connectIdGenerator.generateConnectId())
                .email(application.email())
                .password(application.hashedPassword())
                .firstName(application.firstName())
                .lastName(application.lastName())
                .dateOfBirth(application.dateOfBirth())
                .gender(application.gender())
                .location(application.location())
                .createdAt(Instant.now())
                .build();
    }
}

