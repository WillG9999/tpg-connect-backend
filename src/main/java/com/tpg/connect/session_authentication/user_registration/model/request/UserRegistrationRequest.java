package com.tpg.connect.session_authentication.user_registration.model.request;


import jakarta.validation.constraints.NotNull;

public record UserRegistrationRequest(
        @NotNull String email,
        @NotNull String password,
        @NotNull String firstName,
        @NotNull String lastName,
        @NotNull String dateOfBirth,
        @NotNull String gender,
        @NotNull String location
) {
}
