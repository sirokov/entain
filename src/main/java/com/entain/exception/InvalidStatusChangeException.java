package com.entain.exception;

import static com.entain.config.EntainConstant.INVALID_STATUS_CHANGE_ATTEMPT;

public class InvalidStatusChangeException extends RuntimeException {
    public InvalidStatusChangeException() {
        super(INVALID_STATUS_CHANGE_ATTEMPT);
    }

    public InvalidStatusChangeException(String message) {
        super(message);
    }
}
