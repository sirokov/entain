package com.entain.event;

import com.entain.data.EventStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event representing a status change of a {@link com.entain.data.SportEvent}.
 * <p>
 * This event is published whenever the status of a SportEvent changes (e.g., INACTIVE → ACTIVE, ACTIVE → FINISHED).
 * It contains all relevant information about the event so that listeners can send updates to clients or other systems.
 */
public record SportEventStatusChanged(
        UUID eventId,
        EventStatus newStatus,
        String name,
        String sport,
        LocalDateTime startTime
) implements DomainEvent {}
