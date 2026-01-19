package com.tpg.connect.password_reset.exceptions;

public class TokenAlreadyUsedException extends PasswordResetException {

    public TokenAlreadyUsedException(String token) {
        super("Password reset token has already been used: " + token);
    }
}

