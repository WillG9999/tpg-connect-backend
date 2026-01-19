package com.tpg.connect.login_logout.service;

import com.tpg.connect.login_logout.model.response.LoginResponse;

public interface LoginServiceApi {
    LoginResponse login(String email, String base64EncodedPassword);
}

