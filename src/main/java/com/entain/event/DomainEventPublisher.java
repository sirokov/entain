package com.entain.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Publishes domain events to the Spring application context.
 * <p>
 * This component decouples the event producers (e.g., services) from the event consumers (e.g., SSE listeners, message queues).
 * Any class can inject this publisher and call {@link #publish(DomainEvent)} to notify interested listeners about a domain event.
 */
@Component
public class DomainEventPublisher {

    private final ApplicationEventPublisher springPublisher;


    /**
     * Constructs the DomainEventPublisher with the given Spring ApplicationEventPublisher.
     *
     * @param springPublisher Spring's ApplicationEventPublisher used to broadcast events
     */
    public DomainEventPublisher(ApplicationEventPublisher springPublisher) {
        this.springPublisher = springPublisher;
    }


    /**
     * Publishes a domain event to all registered listeners.
     *
     * @param event the domain event to publish
     */
    public void publish(DomainEvent event) {
        springPublisher.publishEvent(event);
    }
}
