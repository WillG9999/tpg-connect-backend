package com.tpg.connect.email_verification.model.request;

public record SendVerificationCodeRequest(
        String email,
        String userName
) {
}
