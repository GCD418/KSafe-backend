package com.ksafe.backend.domain.exception;

public class InvalidEnrollmentTokenException extends RuntimeException {

    public InvalidEnrollmentTokenException(String message) {
        super(message);
    }
}
