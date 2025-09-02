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
    private SportEvent event;

    @BeforeEach
    void setUp() {
        rule = new StatusChangeRule();
        event = new SportEvent();
        event.setId(UUID.randomUUID());
        event.setSport(FOOTBALL);
        event.setStartTime(LocalDateTime.now().plusHours(1));
    }

    @Test
    void inactiveToActive_futureStart_shouldPass() {
        event.setStatus(EventStatus.INACTIVE);
        assertDoesNotThrow(() -> rule.validate(event, EventStatus.ACTIVE));
    }

    @Test
    void inactiveToActive_pastStart_shouldThrow() {
        event.setStatus(EventStatus.INACTIVE);
        event.setStartTime(LocalDateTime.now().minusHours(1));

        assertThrows(InvalidStatusChangeException.class,
                () -> rule.validate(event, EventStatus.ACTIVE));
    }

    @Test
    void inactiveToFinished_shouldThrow() {
        event.setStatus(EventStatus.INACTIVE);

        assertThrows(InvalidStatusChangeException.class,
                () -> rule.validate(event, EventStatus.FINISHED));
    }

    @Test
    void activeToFinished_shouldPass() {
        event.setStatus(EventStatus.ACTIVE);

        assertDoesNotThrow(() -> rule.validate(event, EventStatus.FINISHED));
    }

    @Test
    void activeToInactive_shouldThrow() {
        event.setStatus(EventStatus.ACTIVE);

        assertThrows(InvalidStatusChangeException.class,
                () -> rule.validate(event, EventStatus.INACTIVE));
    }

    @Test
    void finishedToAny_shouldThrow() {
        event.setStatus(EventStatus.FINISHED);

        assertThrows(InvalidStatusChangeException.class,
                () -> rule.validate(event, EventStatus.INACTIVE));
        assertThrows(InvalidStatusChangeException.class,
                () -> rule.validate(event, EventStatus.ACTIVE));
        assertThrows(InvalidStatusChangeException.class,
                () -> rule.validate(event, EventStatus.FINISHED));
    }

}