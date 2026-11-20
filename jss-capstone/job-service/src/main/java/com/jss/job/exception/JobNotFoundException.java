package com.jss.job.exception;

/**
 * Thrown when a job opening is queried by ID and does not exist in MySQL ('job_db').
 * Results in HTTP 404 (Not Found).
 */
public class JobNotFoundException extends RuntimeException {

    public JobNotFoundException(String message) {
        super(message);
    }

    public JobNotFoundException(Long id) {
        super("Job opening not found with ID: " + id);
    }
}
