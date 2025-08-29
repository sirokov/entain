package com.entain.service;

import com.entain.data.EventStatus;
import com.entain.data.SportEvent;
import com.entain.data.access.SportEventDAO;
import com.entain.exception.EventNotFoundException;
import com.entain.exception.InvalidStatusChangeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SportEventServiceTest {

    @Mock
    private SportEventDAO repository;

    @Mock
    private SseEmitterService sseEmitterService;

    @InjectMocks
    private SportEventService service;

    private SportEvent event;

    @BeforeEach
    void setUp() {
        event = new SportEvent();
        event.setId(UUID.randomUUID());
        event.setSport("FOOTBALL");
        event.setStartTime(LocalDateTime.now().plusHours(2));
        event.setStatus(EventStatus.INACTIVE);
    }

    @Test
    void cannotChange_fromInactiveToFinished() {
        event.setStatus(EventStatus.INACTIVE);
        when(repository.findById(event.getId())).thenReturn(Optional.of(event));

        assertThrows(InvalidStatusChangeException.class,
                () -> service.changeStatus(event.getId(), EventStatus.FINISHED));

        verify(repository, never()).updateStatus(any(), any());
        verifyNoInteractions(sseEmitterService);
    }

    @Test
    void cannotActivate_ifStartTimeInPast() {
        event.setStatus(EventStatus.INACTIVE);
        event.setStartTime(LocalDateTime.now().minusHours(1));
        when(repository.findById(event.getId())).thenReturn(Optional.of(event));

        assertThrows(InvalidStatusChangeException.class,
                () -> service.changeStatus(event.getId(), EventStatus.ACTIVE));

        verify(repository, never()).updateStatus(any(), any());
        verifyNoInteractions(sseEmitterService);
    }

    @Test
    void cannotChange_fromFinishedToAnyStatus() {
        event.setStatus(EventStatus.FINISHED);
        when(repository.findById(event.getId())).thenReturn(Optional.of(event));

        assertThrows(InvalidStatusChangeException.class,
                () -> service.changeStatus(event.getId(), EventStatus.ACTIVE));

        verify(repository, never()).updateStatus(any(), any());
        verifyNoInteractions(sseEmitterService);
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

}