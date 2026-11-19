package com.jss.jobseeker.exception;

/**
 * Thrown when attempting to register or update a candidate with an email
 * already owned by another candidate.
 * Results in HTTP 409 (Conflict).
 */
public class DuplicateJobSeekerException extends RuntimeException {

    public DuplicateJobSeekerException(String message) {
        super(message);
    }

    public DuplicateJobSeekerException(String email, Long existingId) {
        super("Candidate already exists with email: " + email + " (Candidate ID: " + existingId + ")");
    }
}
