package com.github.devfrogora.service.exception;


// Base exception for business logic issues
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}




