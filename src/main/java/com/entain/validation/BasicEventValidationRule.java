package com.entain.validation;

import com.entain.data.EventStatus;
import com.entain.data.SportEvent;

/**
 * Simplified variant of {@link EventValidationRule}.
 * <p>
 * This interface is intended for rules that only depend on the event itself
 * (e.g. checking if the sport type is supported) and do not care about the
 * target {@link EventStatus}.
 * </p>
 *
 * <p>
 * It adapts {@link #validate(SportEvent)} into the more general
 * {@link EventValidationRule#validate(SportEvent, EventStatus)} contract.
 * </p>
 */
public interface BasicEventValidationRule extends EventValidationRule {

    @Override
    default void validate(SportEvent event, EventStatus newStatus) {
        validate(event);
    }

    /**
     * Validate the given {@link SportEvent} without considering its status transition.
     *
     * @param event the sport event being validated
     * @throws com.entain.exception.InvalidStatusChangeException if the event violates the rule
     */
    void validate(SportEvent event);
}