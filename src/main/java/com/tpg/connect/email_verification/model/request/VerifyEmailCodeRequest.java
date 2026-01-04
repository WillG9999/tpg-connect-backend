package com.tpg.connect.email_verification.model.request;

import jakarta.validation.constraints.NotNull;

public record VerifyEmailCodeRequest(
        String email,
       @NotNull String verificationCode
) {
}
