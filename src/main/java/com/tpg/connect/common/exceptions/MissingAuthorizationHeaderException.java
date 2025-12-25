package com.tpg.connect.common.exceptions;

public class MissingAuthorizationHeaderException extends RuntimeException {
    public MissingAuthorizationHeaderException() {
        super("Missing or invalid Authorization header");
    }
}
