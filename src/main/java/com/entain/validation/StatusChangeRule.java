package com.entain.validation;

import com.entain.data.EventStatus;
import com.entain.data.SportEvent;
import com.entain.exception.InvalidStatusChangeException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Validation rule that ensures a {@link SportEvent} transitions only
 * through valid {@link EventStatus} states.
 * <p>
 * Rules enforced:
 * <ul>
 *   <li>INACTIVE → cannot transition directly to FINISHED.</li>
 *    <li>INACTIVE → can become ACTIVE only if start time is <b>not in the past</b>.</li>
 *   <li>ACTIVE → can only transition to FINISHED.</li>
 *   <li>FINISHED → cannot transition to any other state.</li>
 * </ul>
 *
 * <p>Throws {@link InvalidStatusChangeException} if a forbidden transition is attempted.</p>
 */
@Component
public class StatusChangeRule implements EventValidationRule {

    @Override
    public void validate(SportEvent event, EventStatus newStatus) {
        switch (event.getStatus()) {
            case INACTIVE -> {
                if (newStatus == EventStatus.FINISHED)
                    throw new InvalidStatusChangeException();
                if (newStatus == EventStatus.ACTIVE && event.getStartTime().isBefore(LocalDateTime.now()))
                    throw new InvalidStatusChangeException();
            }
            case ACTIVE -> {
                if (newStatus != EventStatus.FINISHED)
                    throw new InvalidStatusChangeException();
            }
            case FINISHED -> throw new InvalidStatusChangeException();
        }
    }
}

