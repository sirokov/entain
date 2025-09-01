package com.entain.controller;

import com.entain.data.EventStatus;
import com.entain.data.SportEvent;
import com.entain.dto.CreateEventRequest;
import com.entain.dto.ErrorResponse;
import com.entain.dto.SportEventResponse;
import com.entain.dto.UpdateStatusRequest;
import com.entain.service.SportEventService;
import com.entain.service.SportValidationService;
import com.entain.service.SseEmitterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/events")
@Tag(name = "Sport Events API", description = "CRUD API for managing sport events with subscription to updates")
public class SportEventController {

    private final SportEventService service;
    private final SseEmitterService sseEmitterService;

    public SportEventController(SportEventService service, SseEmitterService sseEmitterService) {
        this.service = service;
        this.sseEmitterService = sseEmitterService;
    }


    @Operation(
            summary = "Create a new sport event",
            description = "Creates a new sport event with INACTIVE status and provided details",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Event created successfully",
                            content = @Content(schema = @Schema(implementation = SportEventResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @PostMapping
    public SportEventResponse createEvent(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Event details to create",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateEventRequest.class))
            )
            @RequestBody CreateEventRequest request
    ) {
        SportEvent event = new SportEvent(
                null,
                request.getName(),
                request.getSport(),
                EventStatus.INACTIVE,
                request.getStartTime()
        );
        return SportEventResponse.from(service.createEvent(event));
    }


    @Operation(
            summary = "Get list of sport events",
            description = "Retrieve list of sport events with optional filtering by status and sport type"
    )
    @GetMapping
    public List<SportEventResponse> getEvents(
            @Parameter(description = "Optional event status filter")
            @RequestParam Optional<EventStatus> status,

            @Parameter(description = "Optional sport type filter (e.g. FOOTBALL, HOCKEY)")
            @RequestParam Optional<String> sport
    ) {
        return service.getEvents(status.orElse(null), sport.orElse(null)).stream()
                .map(SportEventResponse::from)
                .toList();
    }


    @Operation(
            summary = "Get sport event by ID",
            description = "Retrieve a single sport event by its unique identifier",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Event found",
                            content = @Content(schema = @Schema(implementation = SportEventResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Event not found",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @GetMapping("/{id}")
    public SportEventResponse getEventById(
            @Parameter(description = "UUID of the event", required = true)
            @PathVariable UUID id
    ) {
        return SportEventResponse.from(service.getEventById(id));
    }


    @Operation(
            summary = "Change sport event status",
            description = "Update status of an event. Allowed transitions: INACTIVE→ACTIVE, ACTIVE→FINISHED",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Status updated",
                            content = @Content(schema = @Schema(implementation = SportEventResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid status change",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Event not found",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @PatchMapping("/{id}/status")
    public SportEventResponse changeStatus(
            @Parameter(description = "UUID of the event", required = true)
            @PathVariable UUID id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "New status for the event",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UpdateStatusRequest.class))
            )
            @RequestBody UpdateStatusRequest request
    ) {
        return SportEventResponse.from(service.changeStatus(id, request.status()));
    }


    @Operation(
            summary = "Subscribe to event updates",
            description = "Subscribe via Server-Sent Events (SSE) to receive event status updates in real-time",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Subscribed successfully (SSE stream opened)")
            }
    )
    @CrossOrigin(origins = "*")
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe() {
        return sseEmitterService.createEmitter();
    }

}
