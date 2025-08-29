package com.entain.exception;

import java.util.UUID;
import static com.entain.config.EntainConstant.NOT_FOUND;
import static com.entain.config.EntainConstant.SPORT_EVENT;

public class EventNotFoundException extends RuntimeException {
    public EventNotFoundException(UUID id) {
        super(SPORT_EVENT + id + NOT_FOUND);
    }
}
