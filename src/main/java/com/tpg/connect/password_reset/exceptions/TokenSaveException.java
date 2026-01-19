package com.tpg.connect.password_reset.exceptions;

public class TokenSaveException extends PasswordResetException {

    public TokenSaveException(String email, Throwable cause) {
        super("Failed to save password reset token for email: " + email, cause);
    }
}

