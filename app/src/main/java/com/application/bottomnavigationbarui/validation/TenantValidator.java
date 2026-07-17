package com.application.bottomnavigationbarui.validation;

import android.text.TextUtils;

public class TenantValidator {

    public static ValidationResult validate(String name, String phone, String email) {
        ValidationResult result = new ValidationResult();

        if (name == null || name.trim().isEmpty()) {
            result.addError("Tenant name cannot be empty.");
        } else if (name.trim().length() < 2) {
            result.addError("Tenant name must be at least 2 characters long.");
        }

        if (phone == null || phone.trim().isEmpty()) {
            result.addError("Phone number is required.");
        } else {
            String sanitizedPhone = phone.replaceAll("[^0-9]", "");
            if (sanitizedPhone.length() < 10 || sanitizedPhone.length() > 15) {
                result.addError("Please enter a valid phone number (10-15 digits).");
            }
        }

        if (email != null && !email.trim().isEmpty()) {
            String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
            if (!email.trim().matches(emailPattern)) {
                result.addError("Provided email address format is invalid.");
            }
        }

        return result;
    }
}