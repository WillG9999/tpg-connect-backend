package com.tpg.connect.password_reset.exceptions;

public class TokenExpiredException extends PasswordResetException {

    public TokenExpiredException(String token) {
        super("Password reset token has expired: " + token);
    }
}

