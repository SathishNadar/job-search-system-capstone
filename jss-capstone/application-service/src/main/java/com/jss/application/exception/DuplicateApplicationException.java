package com.jss.application.exception;

public class DuplicateApplicationException extends RuntimeException {

    public DuplicateApplicationException(Long jobId, Long jobSeekerId) {
        super("Candidate ID: " + jobSeekerId + " has already submitted an application for Job ID: " + jobId);
    }
}
