package com.jss.messaging.exception;

public class EmployerNotFoundException extends RuntimeException {

    public EmployerNotFoundException(Long id) {
        super("Employer not found with ID: " + id);
    }
}
