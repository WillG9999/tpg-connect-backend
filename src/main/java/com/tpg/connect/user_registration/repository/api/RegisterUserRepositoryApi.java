package com.tpg.connect.user_registration.repository.api;

import com.tpg.connect.user_registration.model.entity.RegisteredUser;

public interface RegisterUserRepositoryApi {
    boolean saveUser(RegisteredUser user);
}
