package com.entain.service;

import com.entain.data.EventStatus;
import com.entain.data.SportEvent;
import com.entain.data.access.SportEventDAO;
import com.entain.event.DomainEventPublisher;
import com.entain.event.SportEventCreated;
import com.entain.event.SportEventStatusChanged;
import com.entain.exception.EventNotFoundException;
import com.entain.validation.EventValidationRule;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SportEventService {

    private final SportEventDAO sportEventDAO;
    private final DomainEventPublisher eventPublisher;
    private final List<EventValidationRule> validationRules;

    public SportEventService(SportEventDAO sportEventDAO,
                             DomainEventPublisher eventPublisher, List<EventValidationRule> validationRules) {
        this.sportEventDAO = sportEventDAO;
        this.eventPublisher = eventPublisher;
        this.validationRules = validationRules;
    }

    public SportEvent createEvent(SportEvent event) {

        for (EventValidationRule rule : validationRules) {
            rule.validate(event, null);
        }

        event.setId(UUID.randomUUID());
        sportEventDAO.save(event);
        eventPublisher.publish(new SportEventCreated(event));
        return event;
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

        sportEventDAO.updateStatus(id, newStatus);
        event.setStatus(newStatus);

        eventPublisher.publish(new SportEventStatusChanged(
                event.getId(),
                newStatus,
                event.getName(),
                event.getSport(),
                event.getStartTime()
        ));
        return event;
    }
}
