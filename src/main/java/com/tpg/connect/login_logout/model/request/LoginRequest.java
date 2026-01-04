package com.tpg.connect.login_logout.model.request;

import jakarta.validation.constraints.NotNull;

public record LoginRequest(
       @NotNull String email,
       @NotNull String password
) {
}
