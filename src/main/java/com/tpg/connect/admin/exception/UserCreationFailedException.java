package com.tpg.connect.admin.exception;

public class UserCreationFailedException extends RuntimeException {

    public UserCreationFailedException(String applicationId) {
        super("Failed to create user for application: " + applicationId);
    }
}

