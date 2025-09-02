package com.entain.dto;

import com.entain.data.EventStatus;
import com.entain.data.SportEvent;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Response with sport event details")
public record SportEventResponse(
        @Schema(description = "Unique identifier of the event", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,

        @Schema(description = "Name of the event", example = "Champions League Final")
        String name,

        @Schema(description = "Sport type of the event", example = "FOOTBALL")
        String sport,

        @Schema(description = "Current status of the event", example = "ACTIVE")
        EventStatus status,

        @Schema(description = "Start time of the event", example = "2025-09-01T20:00:00")
        LocalDateTime startTime
) {
    public static SportEventResponse from(SportEvent event) {
        return new SportEventResponse(event.id(), event.name(), event.sport(), event.status(), event.startTime());
    }
}