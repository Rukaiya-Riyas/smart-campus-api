package com.smartcampus.exception;

public class LinkedResourceNotFoundException extends RuntimeException {
    private final String roomId;

    public LinkedResourceNotFoundException(String roomId) {
        super("Room with ID '" + roomId + "' does not exist");
        this.roomId = roomId;
    }

    public String getRoomId() {
        return roomId;
    }
}