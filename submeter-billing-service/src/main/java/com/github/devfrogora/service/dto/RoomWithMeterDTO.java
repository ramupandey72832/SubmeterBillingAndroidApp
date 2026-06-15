package com.github.devfrogora.service.dto;

// File: submeter-billing-service/.../service/dto/RoomWithMeterDTO.java
public class RoomWithMeterDTO {
    private RoomDTO room;
    private SubmeterDTO submeter;

    public RoomWithMeterDTO(RoomDTO room, SubmeterDTO submeter) {
        this.room = room;
        this.submeter = submeter;
    }

    public RoomDTO getRoom() { return room; }
    public SubmeterDTO getSubmeter() { return submeter; }
}