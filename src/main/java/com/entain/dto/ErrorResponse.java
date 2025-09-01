package com.entain.dto;

import org.springframework.http.HttpStatus;

public record ErrorResponse(
        String message,
        int status
) {
    public static ErrorResponse of(String message, HttpStatus status) {
        return new ErrorResponse(message, status.value());
    }
}