package com.github.devfrogora.service.exception;


// Specific exception for missing entities
public class ResourceNotFoundException extends BusinessRuleException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}