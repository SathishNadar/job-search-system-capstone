package com.jss.jobseeker.exception;

/**
 * Thrown when a job seeker / candidate entity is queried by ID and does not
 * exist in MySQL.
 * Results in HTTP 404 (Not Found).
 */
public class JobSeekerNotFoundException extends RuntimeException {

    public JobSeekerNotFoundException(String message) {
        super(message);
    }

    public JobSeekerNotFoundException(Long id) {
        super("Candidate not found with ID: " + id);
    }
}
