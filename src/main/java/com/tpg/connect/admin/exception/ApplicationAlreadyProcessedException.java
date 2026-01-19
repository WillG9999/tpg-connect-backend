package com.tpg.connect.admin.exception;

public class ApplicationAlreadyProcessedException extends RuntimeException {

    public ApplicationAlreadyProcessedException(String applicationId, String currentStatus) {
        super("Application " + applicationId + " has already been processed with status: " + currentStatus);
    }
}

