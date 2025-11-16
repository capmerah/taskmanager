package com.theawesomeengineer.taskmanager.api;

import java.time.Instant;

public class ErrorResponse {
    private String message;
    private Instant timestamp;
    private String details;

    public ErrorResponse(String message, String details) {
        this.message = message;
        this.timestamp = Instant.now();
        this.details = details;
    }

    public String getMessage() { return message; }
    public Instant getTimestamp() { return timestamp; }
    public String getDetails() { return details; }
}
