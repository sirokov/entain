package com.entain.dto;

import com.entain.data.SportType;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for creating a new sport event")
public class CreateEventRequest {
    @Schema(description = "Name of the event", example = "Champions League Final")
    private String name;

    @Schema(description = "Sport type of the event", example = "FOOTBALL")
    private String sport;

    @Schema(description = "Start time of the event in ISO format", example = "2025-09-01T20:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;
}