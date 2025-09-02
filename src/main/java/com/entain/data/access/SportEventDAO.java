package com.entain.data.access;

import com.entain.data.EventStatus;
import com.entain.data.SportEvent;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Data Access Object (DAO) interface for managing SportEvent entities.
 *
 * <p>This interface abstracts the persistence layer for sport events, allowing
 * the service layer (SportEventService) to interact with event data without
 * being coupled to a specific storage implementation.</p>
 *
 * <p>Key purposes:</p>
 * <ul>
 *     <li>Decouple business logic from persistence mechanism.</li>
 *     <li>Allow multiple implementations, e.g., in-memory for testing
 *         or database-backed for production.</li>
 *     <li>Support future extensibility without modifying service layer code.</li>
 * </ul>
 */
public interface SportEventDAO {
    SportEvent save(SportEvent event);
    Optional<SportEvent> findById(UUID id);
    List<SportEvent> findAll(EventStatus status, String sport);
    void updateStatus(UUID id, EventStatus newStatus);
}
