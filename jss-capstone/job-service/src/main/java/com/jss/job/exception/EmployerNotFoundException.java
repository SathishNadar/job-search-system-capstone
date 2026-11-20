package com.jss.job.exception;

/**
 * Thrown when an employerId supplied in a job posting does not exist in 'employer-service'.
 * Detected via Spring Cloud OpenFeign inter-service verification.
 * Results in HTTP 404 (Not Found).
 */
public class EmployerNotFoundException extends RuntimeException {

    public EmployerNotFoundException(String message) {
        super(message);
    }

    public EmployerNotFoundException(Long employerId) {
        super("Hiring organization / Employer not found with ID: " + employerId + ". Cannot create or update job for non-existent employer.");
    }
}
