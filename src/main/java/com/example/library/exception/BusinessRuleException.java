package com.example.library.exception;

/** Thrown when a business rule in the Service layer is violated. */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
