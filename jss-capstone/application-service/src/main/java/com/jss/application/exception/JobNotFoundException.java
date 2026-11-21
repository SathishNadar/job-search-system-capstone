package com.jss.application.exception;

public class JobNotFoundException extends RuntimeException {

    public JobNotFoundException(Long id) {
        super("Job opening not found with ID: " + id);
    }
}
