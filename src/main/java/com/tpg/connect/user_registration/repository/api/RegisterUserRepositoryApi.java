package com.tpg.connect.user_registration.repository.api;

import com.tpg.connect.user_registration.model.entity.RegisteredUser;

import java.util.Optional;

public interface RegisterUserRepositoryApi {
    boolean saveUser(RegisteredUser user);
    Optional<RegisteredUser> findByEmail(String email);
    boolean updatePassword(String email, String hashedPassword);
}
