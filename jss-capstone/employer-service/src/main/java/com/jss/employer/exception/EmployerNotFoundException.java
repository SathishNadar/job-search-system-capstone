package com.jss.employer.exception;

/**
 * Thrown when an employer entity is looked up by ID or criteria and does not exist.
 * Results in HTTP 404 (Not Found).
 */
public class EmployerNotFoundException extends RuntimeException {

    public EmployerNotFoundException(String message) {
        super(message);
    }

    public EmployerNotFoundException(Long id) {
        super("Employer not found with ID: " + id);
    }
}
