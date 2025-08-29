package com.entain.data.access;

import com.entain.data.EventStatus;
import com.entain.data.SportEvent;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemorySportEventStore implements SportEventDAO {
    private final Map<UUID, SportEvent> store = new ConcurrentHashMap<>();

    @Override
    public SportEvent save(SportEvent event) {
        store.put(event.getId(), event);
        return event;
    }

    @Override
    public Optional<SportEvent> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<SportEvent> findAll(EventStatus status, String sport) {
        return store.values().stream()
                .filter(e -> status == null || e.getStatus() == status)
                .filter(e -> sport == null || e.getSport().equalsIgnoreCase(sport))
                .toList();
    }

    @Override
    public void updateStatus(UUID id, EventStatus newStatus) {
        SportEvent event = store.get(id);
        if (event != null) event.setStatus(newStatus);
    }
}
