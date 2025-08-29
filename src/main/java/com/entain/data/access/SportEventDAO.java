package com.entain.data.access;

import com.entain.data.EventStatus;
import com.entain.data.SportEvent;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SportEventDAO {
    SportEvent save(SportEvent event);
    Optional<SportEvent> findById(UUID id);
    List<SportEvent> findAll(EventStatus status, String sport);
    void updateStatus(UUID id, EventStatus newStatus);
}
