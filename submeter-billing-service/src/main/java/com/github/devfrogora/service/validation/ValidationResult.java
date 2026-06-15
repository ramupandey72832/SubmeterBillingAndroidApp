// File: submeter-billing-service/.../service/validation/ValidationResult.java
package com.github.devfrogora.service.validation;

public class ValidationResult {
    private final boolean isValid;
    private final String errorMessage;

    public ValidationResult(boolean isValid, String errorMessage) {
        this.isValid = isValid;
        this.errorMessage = errorMessage;
    }

    public static ValidationResult valid() {
        return new ValidationResult(true, null);
    }

    public static ValidationResult invalid(String message) {
        return new ValidationResult(false, message);
    }

    public boolean isValid() { return isValid; }
    public String getErrorMessage() { return errorMessage; }
}