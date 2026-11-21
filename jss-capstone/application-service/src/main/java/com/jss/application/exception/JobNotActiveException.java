package com.jss.application.exception;

public class JobNotActiveException extends RuntimeException {

    public JobNotActiveException(Long jobId, String status) {
        super("Cannot apply to job opening ID: " + jobId + " because its current status is '" + status
                + "' (must be 'ACTIVE')");
    }
}
