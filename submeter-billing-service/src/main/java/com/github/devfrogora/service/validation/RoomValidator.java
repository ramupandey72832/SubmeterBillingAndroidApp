// File: submeter-billing-service/.../service/validation/RoomValidator.java
package com.github.devfrogora.service.validation;

import com.github.devfrogora.service.dto.RoomDTO;

public class RoomValidator {
    public static ValidationResult validateRoom(RoomDTO room) {
        if (room == null) {
            return ValidationResult.invalid("Room data cannot be empty");
        }
        if (room.getRoomNumber() == null || room.getRoomNumber().trim().isEmpty()) {
            return ValidationResult.invalid("Room number is required");
        }
        return ValidationResult.valid();
    }
}