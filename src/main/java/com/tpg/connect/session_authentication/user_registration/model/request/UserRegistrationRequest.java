package com.tpg.connect.session_authentication.user_registration.model.request;

public record UserRegistrationRequest(
        String email,
        String password,
        String firstName,
        String lastName,
        String dateOfBirth,
        String gender,
        String location
) {
}
