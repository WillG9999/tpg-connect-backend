package com.tpg.connect.admin.exception;

public class ApplicationNotFoundException extends RuntimeException {

    public ApplicationNotFoundException(String applicationId) {
        super("Application not found: " + applicationId);
    }
}

