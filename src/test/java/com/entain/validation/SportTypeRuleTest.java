package com.entain.validation;

import com.entain.config.SportsConfig;
import com.entain.data.EventStatus;
import com.entain.data.SportEvent;
import com.entain.exception.InvalidStatusChangeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SportTypeRuleTest {

    private static final String CRICKET = "CRICKET";
    private static final String SWIMMING = "SWIMMING";
    private static final String FOOTBALL = "FOOTBALL";
    private static final String FOO = "FOO";

    private SportTypeRule rule;
    private SportsConfig config;

    @BeforeEach
    void setUp() {
        config = new SportsConfig();
        config.setExtraTypes(List.of(CRICKET, SWIMMING));
        rule = new SportTypeRule(config);
    }

    private SportEvent createEvent(String sport) {
        return new SportEvent(
                UUID.randomUUID(),
                "Test Match",
                sport,
                EventStatus.INACTIVE,
                LocalDateTime.now().plusHours(1)
        );
    }

    @Test
    void validate_validSport_fromEnum_shouldPass() {
        SportEvent event = createEvent(FOOTBALL);
        assertDoesNotThrow(() -> rule.validate(event));
    }

    @Test
    void validate_validSport_fromConfig_shouldPass() {
        SportEvent event = createEvent(CRICKET);
        assertDoesNotThrow(() -> rule.validate(event));
    }

    @Test
    void validate_invalidSport_shouldThrow() {
        SportEvent event = createEvent(FOO);

        InvalidStatusChangeException ex = assertThrows(
                InvalidStatusChangeException.class,
                () -> rule.validate(event)
        );

        assertTrue(ex.getMessage().contains(FOO));
    }

    @Test
    void validate_nullSport_shouldThrow() {
        SportEvent event = createEvent(null);

        InvalidStatusChangeException ex = assertThrows(
                InvalidStatusChangeException.class,
                () -> rule.validate(event)
        );

        assertTrue(ex.getMessage().contains("null"));
    }

    @Test
    void validate_blankSport_shouldThrow() {
        SportEvent event = createEvent(" ");

        InvalidStatusChangeException ex = assertThrows(
                InvalidStatusChangeException.class,
                () -> rule.validate(event)
        );

        assertTrue(ex.getMessage().contains(" "));
    }
}