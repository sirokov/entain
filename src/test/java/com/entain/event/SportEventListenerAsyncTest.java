package com.entain.event;

import com.entain.data.EventStatus;
import com.entain.data.SportEvent;
import com.entain.service.SseEmitterService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SportEventListenerAsyncTest {

    @InjectMocks
    private SportEventSseListener sportEventSseListener;

    @Mock
    private SseEmitterService sseEmitterService;


    @Test
    void shouldHandleSportEventCreatedAsync() {
        // given
        SportEvent event = new SportEvent(
                UUID.randomUUID(),
                "Chelsea vs Arsenal",
                "football",
                EventStatus.INACTIVE,
                LocalDateTime.now().plusDays(1)
        );
        SportEventCreated createdEvent = new SportEventCreated(event);

        // when
        sportEventSseListener.onEventCreated(createdEvent);

        // then
        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(sseEmitterService, times(1)).emitUpdate(event);
        });
    }

    @Test
    void shouldHandleSportEventStatusChangedAsync() {
        // given
        UUID id = UUID.randomUUID();
        SportEventStatusChanged statusChanged = new SportEventStatusChanged(
                id,
                EventStatus.ACTIVE,
                "Real vs Barca",
                "football",
                LocalDateTime.now().plusHours(2)
        );

        SportEvent expectedEvent = new SportEvent(
                statusChanged.eventId(),
                statusChanged.name(),
                statusChanged.sport(),
                statusChanged.newStatus(),
                statusChanged.startTime()
        );

        // when
        sportEventSseListener.onEventStatusChanged(statusChanged);

        // then
        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(sseEmitterService, times(1)).emitUpdate(expectedEvent);
        });
    }
}
