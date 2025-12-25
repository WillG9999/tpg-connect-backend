package com.tpg.connect.user_registration.service;

import com.tpg.connect.user_registration.components.ConnectIdGenerator;
import com.tpg.connect.common.services.authentication.JsonWebTokenProviderService;
import com.tpg.connect.user_registration.exceptions.UserRegistrationException;
import com.tpg.connect.user_registration.model.entity.RegisteredUser;
import com.tpg.connect.user_registration.model.entity.request.UserRegistrationRequest;
import com.tpg.connect.user_registration.model.entity.response.UserRegistrationResponse;
import com.tpg.connect.user_registration.repository.RegisterUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class RegisterUserService {

    private final ConnectIdGenerator connectIdGenerator;
    private final RegisterUserRepository registerUserRepository;
    private final JsonWebTokenProviderService jsonWebTokenService;

    public UserRegistrationResponse registerUser(UserRegistrationRequest request) {
        RegisteredUser registeredUser = saveUserToFirestore(request);
        return new UserRegistrationResponse(jsonWebTokenService.generateToken(
                registeredUser.connectId(), registeredUser.email()));
    }

    public RegisteredUser saveUserToFirestore(UserRegistrationRequest request) {
        RegisteredUser registeredUser = RegisteredUser.builder()
                .connectId(connectIdGenerator.generateConnectId())
                .email(request.email())
                .password(request.password())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .dateOfBirth(request.dateOfBirth())
                .gender(request.gender())
                .location(request.location())
                .createdAt(Instant.now())
                .build();

        if (registerUserRepository.saveUser(registeredUser))
            return registeredUser;
        throw new UserRegistrationException("User Registration failed");
    }
}