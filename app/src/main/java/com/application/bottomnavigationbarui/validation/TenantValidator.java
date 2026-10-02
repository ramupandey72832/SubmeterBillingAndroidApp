package com.application.bottomnavigationbarui.validation;

import android.text.TextUtils;

public class TenantValidator {

    public static ValidationResult validate(String name, String aadharNumber , String phone) {
        ValidationResult result = new ValidationResult();

        if (name == null || name.trim().isEmpty()) {
            result.addError("Tenant name cannot be empty.");
        } else if (name.trim().length() < 2) {
            result.addError("Tenant name must be at least 2 characters long.");
        }

        // Aadhaar validation (Checks for a 12-digit number)
        if (aadharNumber == null || aadharNumber.trim().isEmpty()) {
            result.addError("Aadhaar number is required.");
        } else {
            String sanitizedAadhar = aadharNumber.replaceAll("[^0-9]", "");
            if (sanitizedAadhar.length() != 12) {
                result.addError("Aadhaar number must be a valid 12-digit number.");
            }
        }

        if (phone == null || phone.trim().isEmpty()) {
            result.addError("Phone number is required.");
        } else {
            String sanitizedPhone = phone.replaceAll("[^0-9]", "");
            if (sanitizedPhone.length() < 10 || sanitizedPhone.length() > 15) {
                result.addError("Please enter a valid phone number (10-15 digits).");
            }
        }
        String email="Test123@gmail.com";
        if (email != null && !email.trim().isEmpty()) {
            String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
            if (!email.trim().matches(emailPattern)) {
                result.addError("Provided email address format is invalid.");
            }
        }

        return result;
    }
}