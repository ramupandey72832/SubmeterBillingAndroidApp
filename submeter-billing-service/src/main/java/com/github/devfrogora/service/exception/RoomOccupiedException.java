package com.github.devfrogora.service.exception;


// Specific exception for occupancy rules
public class RoomOccupiedException extends BusinessRuleException {
    public RoomOccupiedException(String roomNumber, int tenantId) {
        super("Cannot modify Room " + roomNumber + ". It is currently occupied by Tenant ID: " + tenantId);
    }
}
