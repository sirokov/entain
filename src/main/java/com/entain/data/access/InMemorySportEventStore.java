package com.entain.data.access;

import com.entain.data.EventStatus;
import com.entain.data.SportEvent;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of the {@link SportEventDAO} interface.
 *
 * <p>This implementation stores SportEvent objects in a concurrent hash map,
 * providing thread-safe access for read and write operations.</p>
 *
 * <p>It is suitable for prototyping, testing, or scenarios where persistent storage
 * is not required. For production, a database-backed implementation should be used.</p>
 *
 * <p>Concurrency notes:</p>
 * <ul>
 *     <li>{@link #updateStatus(UUID, EventStatus)} uses {@code computeIfPresent} to atomically
 *         update the status, preventing race conditions in concurrent environments.</li>
 * </ul>
 */
@Repository
public class InMemorySportEventStore implements SportEventDAO {
    private final Map<UUID, SportEvent> store = new ConcurrentHashMap<>();

    @Override
    public SportEvent save(SportEvent event) {
        // Insert event only if the ID is not already present in the store.
        // If another event with the same ID exists, it will not be overwritten.
        // This ensures thread-safe insertion and prevents accidental overwrites
        // when multiple requests with the same ID are processed concurrently.
        SportEvent existing = store.putIfAbsent(event.id(), event);
        return existing != null ? existing : event;
    }

    @Override
    public Optional<SportEvent> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<SportEvent> findAll(EventStatus status, String sport) {
        return store.values().stream()
                .filter(e -> status == null || e.status() == status)
                .filter(e -> sport == null || e.sport().equalsIgnoreCase(sport))
                .toList();
    }

    @Override
    public void updateStatus(UUID id, EventStatus newStatus) {
        // Atomically update the status of the event if it exists
        // Using computeIfPresent ensures thread-safe update, preventing race conditions
        // TODO: When switching to a database-backed implementation, consider using
        //       optimistic locking (versioning) to prevent concurrent update conflicts
        store.computeIfPresent(id, (uuid, oldEvent) -> oldEvent.withStatus(newStatus));
    }
}
