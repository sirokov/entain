package com.entain.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Arrays;
import java.util.List;
import static com.entain.config.EntainConstant.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EventNotFoundException.class)
    public ResponseEntity<String> handleNotFound(EventNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidStatusChangeException.class)
    public ResponseEntity<String> handleInvalidStatus(InvalidStatusChangeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleOther(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleInvalidJson(HttpMessageNotReadableException ex) {
        String message = INVALID_REQUEST_BODY;

        if (ex.getCause() instanceof InvalidFormatException invalidFormatEx) {
            String fieldName = invalidFormatEx.getPath().get(0).getFieldName();
            Class<?> targetType = invalidFormatEx.getTargetType();

            List<String> accepted;
            if (targetType.isEnum()) {
                accepted = Arrays.stream(targetType.getEnumConstants())
                        .map(Object::toString)
                        .toList();
            } else {
                accepted = List.of();
            }

            message = INVALID_VALUE + fieldName + ACCEPTED_VALUES + accepted;
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }

    @ExceptionHandler(AsyncTaskRejectedException.class)
    public ResponseEntity<String> handleAsyncRejected(AsyncTaskRejectedException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(TASK_REJECTED + ex.getMessage());
    }
}
