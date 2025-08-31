package com.entain.event;

/**
 * Marker interface for all domain events in the system.
 * <p>
 * A domain event represents a meaningful change in the state of the domain (e.g., creation or status change of a SportEvent).
 * Implementing classes carry the data relevant to the event.
 * <p>
 * Domain events are published via {@link DomainEventPublisher} and consumed by listeners such as {@link SportEventSseListener}.
 */
public interface DomainEvent {}