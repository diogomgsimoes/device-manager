package com.research.devicemanager.exception;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ErrorResponse {

    private LocalDateTime timestamp;
    private String message;
    private List<String> errors;

    public ErrorResponse(String message, List<String> errors) {
        this.timestamp = LocalDateTime.now();
        this.message = message;
        this.errors = errors;
    }
}