package com.entain.dto;

import com.entain.data.EventStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request for updating event status")
public record UpdateStatusRequest(
        @Schema(description = "New status of the event", example = "ACTIVE")
        EventStatus status
) {}