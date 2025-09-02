package com.entain.validation;

import com.entain.data.EventStatus;
import com.entain.data.SportEvent;

/**
 * Contract for validation rules applied to {@link SportEvent}.
 * <p>
 * Rules implementing this interface can validate an event against both its
 * current and the desired {@link EventStatus}.
 * </p>
 *
 * <p>Typical use cases:</p>
 * <ul>
 *   <li>Preventing invalid status transitions (e.g. from INACTIVE directly to FINISHED).</li>
 *   <li>Ensuring business constraints depending on event lifecycle.</li>
 * </ul>
 */
public interface EventValidationRule {
    /**
     * Validate the given {@link SportEvent} with respect to its potential new status.
     *
     * @param event     the sport event being validated
     * @param newStatus the new status the event is about to transition to
     * @throws com.entain.exception.InvalidStatusChangeException if the event violates the rule
     */
    void validate(SportEvent event, EventStatus newStatus);
}
