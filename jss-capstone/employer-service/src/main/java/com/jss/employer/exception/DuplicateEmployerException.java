package com.jss.employer.exception;

/**
 * Thrown when attempting to register or update an employer with an email that is already registered.
 * Results in HTTP 409 (Conflict).
 */
public class DuplicateEmployerException extends RuntimeException {

    public DuplicateEmployerException(String message) {
        super(message);
    }

    public DuplicateEmployerException(String email, Long existingId) {
        super("Employer already exists with email: " + email + " (Employer ID: " + existingId + ")");
    }
}
