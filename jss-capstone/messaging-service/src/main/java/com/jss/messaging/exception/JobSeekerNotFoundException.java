package com.jss.messaging.exception;

public class JobSeekerNotFoundException extends RuntimeException {

    public JobSeekerNotFoundException(Long id) {
        super("Job seeker not found with ID: " + id);
    }
}
