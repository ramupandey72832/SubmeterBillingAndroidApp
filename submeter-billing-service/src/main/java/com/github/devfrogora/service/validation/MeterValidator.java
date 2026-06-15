// File: submeter-billing-service/.../service/validation/MeterValidator.java
package com.github.devfrogora.service.validation;

import com.github.devfrogora.service.dto.SubmeterDTO;

public class MeterValidator {
    public static ValidationResult validateMeter(SubmeterDTO meter) {
        if (meter == null) {
            return ValidationResult.invalid("Submeter data cannot be empty");
        }
        if (meter.getMeterSerialNumber() == null || meter.getMeterSerialNumber().trim().isEmpty()) {
            return ValidationResult.invalid("Meter serial number is required");
        }
        // Assuming your DTO uses double or int for reading fields
        if (meter.getInitialReading() < 0) {
            return ValidationResult.invalid("Initial meter reading cannot be negative");
        }
        return ValidationResult.valid();
    }
}