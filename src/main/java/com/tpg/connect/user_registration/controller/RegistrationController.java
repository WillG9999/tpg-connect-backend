package com.tpg.connect.user_registration.controller;

import com.tpg.connect.user_registration.controller.api.RegisterApi;
import com.tpg.connect.user_registration.model.dto.BearerTokenDTO;
import com.tpg.connect.user_registration.model.entity.request.UserRegistrationRequest;
import com.tpg.connect.user_registration.service.RegisterUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static com.tpg.connect.common.constants.HeaderConstants.X_AUTHORISATION;

@RestController
@RequiredArgsConstructor
@Slf4j
public class RegistrationController implements RegisterApi {

    private final RegisterUserService registerUserService;
    public ResponseEntity<Void> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        log.info("Registering User :: " + request.email());
        BearerTokenDTO response = registerUserService.registerUser(request);
        return ResponseEntity.ok()
                .header(X_AUTHORISATION,"Bearer " + response.bearer())
                .build();
    }
}
