package com.jss.job.exception;

/**
 * Thrown when an employer attempts to publish a job with a title that they already have
 * active on the platform.
 * Results in HTTP 409 (Conflict).
 */
public class DuplicateJobException extends RuntimeException {

    public DuplicateJobException(String message) {
        super(message);
    }

    public DuplicateJobException(Long employerId, String jobTitle) {
        super(String.format("Employer ID %d already has an active job posting for '%s'. Please close or update the existing job instead of duplicating.", employerId, jobTitle));
    }
}
