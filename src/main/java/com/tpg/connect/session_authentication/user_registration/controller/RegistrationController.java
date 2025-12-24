package com.tpg.connect.session_authentication.user_registration.controller;

import com.tpg.connect.session_authentication.user_registration.controller.api.RegisterApi;
import com.tpg.connect.session_authentication.user_registration.model.request.UserRegistrationRequest;
import com.tpg.connect.session_authentication.user_registration.model.response.UserRegistrationResponse;
import com.tpg.connect.session_authentication.user_registration.service.RegisterUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class RegistrationController implements RegisterApi {

    private final RegisterUserService registerUserService;

    public ResponseEntity<UserRegistrationResponse> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        log.info("Registering User");
        UserRegistrationResponse response = registerUserService.registerUser(request);
        return ResponseEntity.ok(response);
    }
}
