package com.tpg.connect.login_logout.controller.api;

import com.tpg.connect.login_logout.model.request.LoginRequest;
import com.tpg.connect.login_logout.model.response.LoginResponse;
import com.tpg.connect.login_logout.service.LoginServiceApi;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class LoginController implements LoginApi {

    private final LoginServiceApi loginService;

    @Override
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for email: {}", request.email());
        LoginResponse response = loginService.login(request.email(), request.password());
        return ResponseEntity.ok(response);
    }
}
