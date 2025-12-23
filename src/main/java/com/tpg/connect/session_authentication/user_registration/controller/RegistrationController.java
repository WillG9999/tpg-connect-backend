package com.tpg.connect.session_authentication.user_registration.controller;

import com.tpg.connect.session_authentication.user_registration.controller.api.RegisterApi;
import com.tpg.connect.session_authentication.user_registration.model.request.UserRegistrationRequest;
import com.tpg.connect.session_authentication.user_registration.model.response.UserRegistrationResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RegistrationController implements RegisterApi {

    public ResponseEntity<UserRegistrationResponse> registerUser(UserRegistrationRequest request) {
        //TODO: return service Actions - this is temp
        return ResponseEntity.ok(new UserRegistrationResponse("test"));
    }
}
