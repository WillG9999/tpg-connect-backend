package com.tpg.connect.session_authentication.user_registration.repository.api;

import com.tpg.connect.session_authentication.user_registration.model.entity.RegisteredUser;

public interface RegisterUserRepositoryApi {
    boolean saveUser(RegisteredUser user);
}
