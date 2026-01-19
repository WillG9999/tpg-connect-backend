package com.tpg.connect.user_registration.service;

import com.tpg.connect.common.jsonwebtoken.components.JsonWebTokenProvider;
import com.tpg.connect.user_registration.components.ConnectIdGenerator;
import com.tpg.connect.user_registration.exceptions.UserRegistrationException;
import com.tpg.connect.user_registration.model.dto.BearerTokenDTO;
import com.tpg.connect.user_registration.model.entity.RegisteredUser;
import com.tpg.connect.user_registration.model.entity.request.UserRegistrationRequest;
import com.tpg.connect.user_registration.repository.RegisterUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Service
@Slf4j
@RequiredArgsConstructor
public class RegisterUserService {

    private final ConnectIdGenerator connectIdGenerator;
    private final RegisterUserRepository registerUserRepository;
    private final JsonWebTokenProvider jsonWebTokenService;
    private final PasswordEncoder passwordEncoder;

    public BearerTokenDTO registerUser(UserRegistrationRequest request) {
        RegisteredUser registeredUser = saveUserToFirestore(request);
        return new BearerTokenDTO(jsonWebTokenService.generateToken(
                registeredUser.connectId(), registeredUser.email()));
    }

    public RegisteredUser saveUserToFirestore(UserRegistrationRequest request) {
        String decodedPassword = decodeBase64Password(request.password());
        String hashedPassword = passwordEncoder.encode(decodedPassword);

        RegisteredUser registeredUser = RegisteredUser.builder()
                .connectId(connectIdGenerator.generateConnectId())
                .email(request.email())
                .password(hashedPassword)
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

    private String decodeBase64Password(String base64EncodedPassword) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64EncodedPassword);
            return new String(decodedBytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            log.error("Failed to decode base64 password", e);
            throw new UserRegistrationException("Invalid password format");
        }
    }
}