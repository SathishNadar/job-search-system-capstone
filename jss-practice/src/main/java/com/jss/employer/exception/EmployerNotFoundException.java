package com.jss.employer.exception;

public class EmployerNotFoundException extends RuntimeException {

    public EmployerNotFoundException(String message) {
        super(message);
    }

    public EmployerNotFoundException(Long id) {
        super("Employer not found with id: " + id);
    }
}
