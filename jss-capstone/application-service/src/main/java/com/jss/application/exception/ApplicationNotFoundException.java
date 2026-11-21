package com.jss.application.exception;

public class ApplicationNotFoundException extends RuntimeException {

    public ApplicationNotFoundException(Long id) {
        super("Job application not found with ID: " + id);
    }
}
