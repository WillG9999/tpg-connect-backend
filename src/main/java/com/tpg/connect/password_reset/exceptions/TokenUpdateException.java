package com.tpg.connect.password_reset.exceptions;

public class TokenUpdateException extends PasswordResetException {

    public TokenUpdateException(String token, Throwable cause) {
        super("Failed to update password reset token: " + token, cause);
    }
}

