package com.entain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;

@Schema(description = "Standard error response")
public record ErrorResponse(
        @Schema(description = "Error message describing what went wrong", example = "Invalid sport: FOOTBALL2. Accepted values: [FOOTBALL, HOCKEY]")
        String message,

        @Schema(description = "HTTP status code of the error", example = "400")
        int status
) {
    public static ErrorResponse of(String message, HttpStatus status) {
        return new ErrorResponse(message, status.value());
    }
}