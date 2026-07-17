package com.application.bottomnavigationbarui.validation;

public class RoomValidator {

    public static ValidationResult validate(String roomNumber, String roomType, String meterSerial, String initialReadingStr) {
        ValidationResult result = new ValidationResult();

        if (roomNumber == null || roomNumber.trim().isEmpty()) {
            result.addError("Room number layout field is required.");
        }

        if (roomType == null || roomType.trim().isEmpty()) {
            result.addError("Room structural category specification is required.");
        }

        if (meterSerial == null || meterSerial.trim().isEmpty()) {
            result.addError("Assigned submeter identification serial is required.");
        }

        if (initialReadingStr == null || initialReadingStr.trim().isEmpty()) {
            result.addError("Initial baseline meter reading is required.");
        } else {
            try {
                double reading = Double.parseDouble(initialReadingStr);
                if (reading < 0) {
                    result.addError("Initial meter index tracking reading cannot be negative.");
                }
            } catch (NumberFormatException e) {
                result.addError("Initial index tracking reading must be a valid numeric calculation string.");
            }
        }

        return result;
    }
}