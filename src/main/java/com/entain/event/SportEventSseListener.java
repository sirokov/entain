package com.entain.event;

import com.entain.data.SportEvent;
import com.entain.service.SseEmitterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service that listens to domain events related to SportEvent and pushes updates to subscribed clients via SSE.
 * <p>
 * This class acts as a bridge between the domain layer and the SSE delivery mechanism.
 * It is automatically invoked by Spring whenever a domain event is published.
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Listen for {@link SportEventCreated} events and send the full newly created SportEvent to all SSE subscribers.</li>
 *     <li>Listen for {@link SportEventStatusChanged} events and send the updated SportEvent with the new status to all SSE subscribers.</li>
 * </ul>
 * <p>
 * This approach decouples the business logic in {@link com.entain.service.SportEventService} from the delivery mechanism (SSE),
 * making it easier to extend the system in the future (e.g., sending events to Kafka, WebSocket, or other channels).
 */
@Slf4j
@Service
public class SportEventSseListener {

    private final SseEmitterService sseEmitterService;

    public SportEventSseListener(SseEmitterService sseEmitterService) {
        this.sseEmitterService = sseEmitterService;
    }

    /**
     * Handles the {@link SportEventCreated} event.
     * <p>
     * When a new SportEvent is created, this method is automatically invoked by Spring
     * and sends the full SportEvent to all subscribed clients via SSE.
     *
     * @param event the domain event representing the creation of a new SportEvent
     */
    @Async("eventExecutor")
    @EventListener
    public void onEventCreated(SportEventCreated event) {
        if (log.isDebugEnabled()) {
            log.debug("Handling SportEventCreated in thread: {}", Thread.currentThread().getName());
        }
        sseEmitterService.emitUpdate(event.event());
    }

    /**
     * Handles the {@link SportEventStatusChanged} event.
     * <p>
     * When a SportEvent changes its status, this method is automatically invoked by Spring.
     * It constructs a full SportEvent object with the updated status and sends it to all subscribed clients via SSE.
     *
     * @param event the domain event representing the status change of a SportEvent
     */
    @Async("eventExecutor")
    @EventListener
    public void onEventStatusChanged(SportEventStatusChanged event) {
        if (log.isDebugEnabled()) {
            log.debug("Handling SportEventStatusChanged in thread: {}", Thread.currentThread().getName());
        }
        sseEmitterService.emitUpdate(
                new SportEvent(
                        event.eventId(),
                        event.name(),
                        event.sport(),
                        event.newStatus(),
                        event.startTime()
                )
        );
    }
}
