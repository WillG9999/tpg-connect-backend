package com.tpg.connect.user_registration.model.entity.request;


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
