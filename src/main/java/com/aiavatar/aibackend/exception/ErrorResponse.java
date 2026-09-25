package com.aiavatar.aibackend.exception;

import java.time.LocalDateTime;

public class ErrorResponse {

    private String error;
    private String message;
    private LocalDateTime timestamp;
    private int status;

    public ErrorResponse(
            String error,
            String message,
            LocalDateTime timestamp,
            int status) {

        this.error = error;
        this.message = message;
        this.timestamp = timestamp;
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }
}