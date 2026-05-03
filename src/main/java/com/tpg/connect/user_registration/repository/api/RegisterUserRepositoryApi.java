package com.tpg.connect.user_registration.repository.api;

import com.tpg.connect.user_registration.model.entity.RegisteredUser;

import java.util.List;
import java.util.Optional;

public interface RegisterUserRepositoryApi {
    boolean saveUser(RegisteredUser user);
    Optional<RegisteredUser> findByEmail(String email);
    Optional<RegisteredUser> findByConnectId(long connectId);
    List<RegisteredUser> findAll();
    boolean updatePassword(String email, String hashedPassword);
}
