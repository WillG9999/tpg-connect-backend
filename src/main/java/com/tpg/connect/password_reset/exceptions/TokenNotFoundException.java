package com.tpg.connect.password_reset.exceptions;

public class TokenNotFoundException extends PasswordResetException {

    public TokenNotFoundException(String token) {
        super("Password reset token not found: " + token);
    }
}

