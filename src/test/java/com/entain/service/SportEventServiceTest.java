package com.entain.service;

import com.entain.config.SportsConfig;
import com.entain.data.EventStatus;
import com.entain.data.SportEvent;
import com.entain.data.access.SportEventDAO;
import com.entain.event.DomainEventPublisher;
import com.entain.event.SportEventCreated;
import com.entain.exception.EventNotFoundException;
import com.entain.exception.InvalidStatusChangeException;
import com.entain.validation.EventValidationRule;
import com.entain.validation.SportTypeRule;
import com.entain.validation.StatusChangeRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SportEventServiceTest {

    private static final String SPORT = "FOOTBALL";
    private static final String MATCH_A = "Match A";
    private static final String MATCH_B = "Match B";
    private static final String EXISTING_MATCH = "Existing Match";

    @Mock
    private SportEventDAO repository;

    @Mock
    private SseEmitterService sseEmitterService;

    @Mock
    private DomainEventPublisher publisher;

    private SportEventService service;
    private SportEvent baseEvent;

    @BeforeEach
    void setUp() {
        List<EventValidationRule> rules = List.of(
                new StatusChangeRule(),
                new SportTypeRule(new SportsConfig())
        );
        service = new SportEventService(repository, publisher, rules);

        baseEvent = new SportEvent(
                UUID.randomUUID(),
                SPORT,
                SPORT,
                EventStatus.INACTIVE,
                LocalDateTime.now().plusHours(2)
        );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidStatusChanges")
    void invalidStatusChange_shouldThrow(EventStatus currentStatus, EventStatus newStatus, LocalDateTime startTime) {
        SportEvent eventToTest = new SportEvent(
                baseEvent.id(),
                baseEvent.name(),
                baseEvent.sport(),
                currentStatus,
                startTime
        );

        when(repository.findById(eventToTest.id())).thenReturn(Optional.of(eventToTest));

        assertThrows(InvalidStatusChangeException.class,
                () -> service.changeStatus(eventToTest.id(), newStatus));

        verify(repository, never()).updateStatus(any(), any());
        verifyNoInteractions(sseEmitterService);
    }

    static Stream<Arguments> provideInvalidStatusChanges() {
        LocalDateTime future = LocalDateTime.now().plusHours(2);
        LocalDateTime past = LocalDateTime.now().minusHours(1);

        return Stream.of(
                Arguments.of(EventStatus.INACTIVE, EventStatus.FINISHED, future),
                Arguments.of(EventStatus.INACTIVE, EventStatus.ACTIVE, past),
                Arguments.of(EventStatus.FINISHED, EventStatus.ACTIVE, future)
        );
    }

    @Test
    void changeStatus_eventNotFound_throwsEventNotFoundException() {
        UUID missingId = UUID.randomUUID();
        when(repository.findById(missingId)).thenReturn(Optional.empty());

        assertThrows(EventNotFoundException.class,
                () -> service.changeStatus(missingId, EventStatus.ACTIVE));

        verify(repository, never()).updateStatus(any(), any());
        verifyNoInteractions(sseEmitterService);
    }

    @Test
    void createEvent_shouldSaveAndPublish_whenEventIsUnique() {
        SportEvent request = new SportEvent(
                null,
                MATCH_A,
                SPORT,
                EventStatus.INACTIVE,
                LocalDateTime.now().plusHours(1)
        );

        when(repository.save(any(SportEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SportEvent result = service.createEvent(request);

        assertNotNull(result.id());
        assertEquals(request.name(), result.name());
        assertEquals(request.sport(), result.sport());
        assertEquals(request.status(), result.status());
        assertEquals(request.startTime(), result.startTime());

        verify(repository).save(any(SportEvent.class));
        verify(publisher).publish(any(SportEventCreated.class));
    }

    @Test
    void createEvent_shouldThrow_whenEventAlreadyExists() {
        SportEvent request = new SportEvent(
                null,
                MATCH_B,
                SPORT,
                EventStatus.INACTIVE,
                LocalDateTime.now().plusHours(2)
        );

        SportEvent existing = new SportEvent(
                UUID.randomUUID(),
                EXISTING_MATCH,
                SPORT,
                EventStatus.INACTIVE,
                LocalDateTime.now().plusHours(3)
        );

        when(repository.save(any(SportEvent.class))).thenReturn(existing);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.createEvent(request)
        );

        assertTrue(ex.getMessage().contains("already exists"));
        verify(repository).save(any(SportEvent.class));
        verify(publisher, never()).publish(any());
    }

    @Test
    void createEvent_shouldThrow_whenEventIdAlreadyExists() {
        // Arrange: create a request with null ID (service will generate a new UUID)
        SportEvent request = new SportEvent(
                null,
                MATCH_B,
                SPORT,
                EventStatus.INACTIVE,
                LocalDateTime.now().plusHours(1)
        );

        // Simulate repository returning an existing event with a different UUID (conflict)
        SportEvent existingEvent = new SportEvent(
                UUID.randomUUID(), // уже существующий ID
                EXISTING_MATCH,
                SPORT,
                EventStatus.INACTIVE,
                LocalDateTime.now().plusHours(1)
        );

        // Mock save() to return the existing event to simulate ID conflict
        when(repository.save(any(SportEvent.class)))
                .thenReturn(existingEvent);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.createEvent(request));

        // Verify that the exception message clearly indicates a conflict
        assertTrue(ex.getMessage().contains("already exists"));

        // Ensure that the event publisher is NOT called since creation failed
        verify(publisher, never()).publish(any());
    }


}