package com.entain.event;

import com.entain.data.SportEvent;

/**
 * Domain event representing the creation of a new {@link SportEvent}.
 * <p>
 * This event is published after a SportEvent is successfully created.
 * Listeners can use this event to notify clients, update logs, or trigger further processing.
 */
public record SportEventCreated(SportEvent event) implements DomainEvent {}