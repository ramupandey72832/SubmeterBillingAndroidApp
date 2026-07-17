package com.application.bottomnavigationbarui.validation;

public class MeterReadingValidator {

    public static ValidationResult validate(String currentReadingStr, double previousReading) {
        ValidationResult result = new ValidationResult();

        if (currentReadingStr == null || currentReadingStr.trim().isEmpty()) {
            result.addError("Current utility submeter capture value is missing.");
            return result;
        }

        try {
            double currentReading = Double.parseDouble(currentReadingStr);
            if (currentReading < 0) {
                result.addError("Captured metrics value index cannot fall below zero.");
            }
            if (currentReading < previousReading) {
                result.addError("New tracking entry (" + currentReading + ") cannot be smaller than the prior recorded sequence index (" + previousReading + ").");
            }
        } catch (NumberFormatException e) {
            result.addError("Supplied entry parameters do not register as numerical formats.");
        }

        return result;
    }
}