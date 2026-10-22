package com.submeter.commonlib.response;

public class ResourceNotFoundException extends BusinessRuleException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}