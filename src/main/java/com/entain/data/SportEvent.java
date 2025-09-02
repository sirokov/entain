package com.entain.data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a sport event with all its essential properties.
 *
 * <p>This is implemented as a Java record instead of a traditional class to enforce immutability
 * which significantly improves thread-safety when instances are shared between multiple threads.</p>
 *
 * <p>Provides a {@link #withStatus(EventStatus)} method to create a new instance with an updated status,
 * preserving immutability and ensuring thread-safe updates.</p>
 */
public record SportEvent(
        UUID id,
        String name,
        String sport,
        EventStatus status,
        LocalDateTime startTime
) {

    /**
     * Returns a new {@code SportEvent} instance with the given status, keeping all other fields unchanged.
     * This approach ensures that the original instance remains immutable and safe for concurrent access.
     *
     * @param newStatus the new status of the event
     * @return a new SportEvent with updated status
     */
    public SportEvent withStatus(EventStatus newStatus) {
        return new SportEvent(this.id, this.name, this.sport, newStatus, this.startTime);
    }
}