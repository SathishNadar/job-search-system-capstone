package com.jss.application.exception;

public class JobSeekerNotFoundException extends RuntimeException {

    public JobSeekerNotFoundException(Long id) {
        super("Job seeker not found with ID: " + id);
    }
}
