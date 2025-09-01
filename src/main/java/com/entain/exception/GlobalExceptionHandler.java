package com.entain.exception;

import com.entain.dto.ErrorResponse;
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
    public ResponseEntity<ErrorResponse> handleNotFound(EventNotFoundException ex) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(InvalidStatusChangeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStatus(InvalidStatusChangeException ex) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOther(Exception ex) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidJson(HttpMessageNotReadableException ex) {
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
        return buildError(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(AsyncTaskRejectedException.class)
    public ResponseEntity<ErrorResponse> handleAsyncRejected(AsyncTaskRejectedException ex) {
        return buildError(HttpStatus.SERVICE_UNAVAILABLE, TASK_REJECTED + ex.getMessage());
    }

    private ResponseEntity<ErrorResponse> buildError(HttpStatus status, String message) {
        return ResponseEntity
                .status(status)
                .body(ErrorResponse.of(message, status));
    }
}
