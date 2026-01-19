package com.tpg.connect.password_reset.model.request;

import jakarta.validation.constraints.NotBlank;

public record VerifyResetTokenRequest(
        @NotBlank String token
) {
}

