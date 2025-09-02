package com.entain.service;

import com.entain.data.EventStatus;
import com.entain.data.SportEvent;
import com.entain.data.access.SportEventDAO;
import com.entain.event.DomainEventPublisher;
import com.entain.event.SportEventCreated;
import com.entain.event.SportEventStatusChanged;
import com.entain.exception.EventNotFoundException;
import com.entain.validation.EventValidationRule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SportEventService {

    private final SportEventDAO sportEventDAO;
    private final DomainEventPublisher eventPublisher;
    private final List<EventValidationRule> validationRules;

    public SportEvent createEvent(SportEvent event) {
        for (EventValidationRule rule : validationRules) {
            rule.validate(event, null);
        }

        event = new SportEvent(
                UUID.randomUUID(),
                event.name(),
                event.sport(),
                event.status(),
                event.startTime()
        );

        SportEvent stored = sportEventDAO.save(event);

        if (!stored.equals(event)) {
            throw new IllegalStateException(
                    "Event with id " + event.id() + " already exists. Conflict detected."
            );
        }
        eventPublisher.publish(new SportEventCreated(stored));
        return stored;
    }

    public List<SportEvent> getEvents(EventStatus status, String sport) {
        return sportEventDAO.findAll(status, sport);
    }

    public SportEvent getEventById(UUID id) {
        return sportEventDAO.findById(id)
                .orElseThrow(() -> new EventNotFoundException(id));
    }

    public SportEvent changeStatus(UUID id, EventStatus newStatus) {
        SportEvent event = getEventById(id);

        for (EventValidationRule rule : validationRules) {
            rule.validate(event, newStatus);
        }

        // Update status immutably in DAO
        sportEventDAO.updateStatus(id, newStatus);

        // Fetch updated event to return (DAO returns new immutable instance)
        event = sportEventDAO.findById(id)
                .orElseThrow(() -> new EventNotFoundException(id));

        eventPublisher.publish(new SportEventStatusChanged(
                event.id(),
                newStatus,
                event.name(),
                event.sport(),
                event.startTime()
        ));

        return event;
    }
}
