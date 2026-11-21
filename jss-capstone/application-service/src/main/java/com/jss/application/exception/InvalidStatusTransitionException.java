package com.jss.application.exception;

public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(String currentStatus, String targetStatus) {
        super("Invalid application status transition: cannot change status from '" + currentStatus + "' to '"
                + targetStatus + "'");
    }
}
