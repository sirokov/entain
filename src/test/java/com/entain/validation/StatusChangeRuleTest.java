package com.entain.validation;

import com.entain.data.EventStatus;
import com.entain.data.SportEvent;
import com.entain.exception.InvalidStatusChangeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class StatusChangeRuleTest {

    private static final String FOOTBALL = "FOOTBALL";

    private StatusChangeRule rule;
    private UUID eventId;
    private String eventName;
    private String sport;
    private LocalDateTime futureStartTime;

    @BeforeEach
    void setUp() {
        rule = new StatusChangeRule();
        eventId = UUID.randomUUID();
        eventName = "Test Match";
        sport = FOOTBALL;
        futureStartTime = LocalDateTime.now().plusHours(1);
    }

    private SportEvent createEvent(EventStatus status, LocalDateTime startTime) {
        return new SportEvent(eventId, eventName, sport, status, startTime);
    }

    @Test
    void inactiveToActive_futureStart_shouldPass() {
        SportEvent event = createEvent(EventStatus.INACTIVE, futureStartTime);
        assertDoesNotThrow(() -> rule.validate(event, EventStatus.ACTIVE));
    }

    @Test
    void inactiveToActive_pastStart_shouldThrow() {
        SportEvent event = createEvent(EventStatus.INACTIVE, LocalDateTime.now().minusHours(1));
        assertThrows(InvalidStatusChangeException.class,
                () -> rule.validate(event, EventStatus.ACTIVE));
    }

    @Test
    void inactiveToFinished_shouldThrow() {
        SportEvent event = createEvent(EventStatus.INACTIVE, futureStartTime);
        assertThrows(InvalidStatusChangeException.class,
                () -> rule.validate(event, EventStatus.FINISHED));
    }

    @Test
    void activeToFinished_shouldPass() {
        SportEvent event = createEvent(EventStatus.ACTIVE, futureStartTime);
        assertDoesNotThrow(() -> rule.validate(event, EventStatus.FINISHED));
    }

    @Test
    void activeToInactive_shouldThrow() {
        SportEvent event = createEvent(EventStatus.ACTIVE, futureStartTime);
        assertThrows(InvalidStatusChangeException.class,
                () -> rule.validate(event, EventStatus.INACTIVE));
    }

    @Test
    void finishedToAny_shouldThrow() {
        SportEvent event = createEvent(EventStatus.FINISHED, futureStartTime);

        assertThrows(InvalidStatusChangeException.class,
                () -> rule.validate(event, EventStatus.INACTIVE));
        assertThrows(InvalidStatusChangeException.class,
                () -> rule.validate(event, EventStatus.ACTIVE));
        assertThrows(InvalidStatusChangeException.class,
                () -> rule.validate(event, EventStatus.FINISHED));
    }
}