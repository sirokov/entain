package com.entain.exception;

import com.entain.data.EventStatus;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import java.util.List;
import java.util.UUID;
import static com.entain.config.EntainConstant.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setup() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleEventNotFound_returns404() {
        UUID id = UUID.randomUUID();
        EventNotFoundException ex = new EventNotFoundException(id);

        var response = handler.handleNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(SPORT_EVENT + id + NOT_FOUND, response.getBody().message());
    }

    @Test
    void handleInvalidStatus_returns400() {
        InvalidStatusChangeException ex = new InvalidStatusChangeException(INVALID_STATUS_CHANGE_ATTEMPT);

        var response = handler.handleInvalidStatus(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(INVALID_STATUS_CHANGE_ATTEMPT, response.getBody().message());
    }

    @Test
    void handleOther_returns500() {
        RuntimeException ex = new RuntimeException("Some error");

        var response = handler.handleOther(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(INTERNAL_SERVER_ERROR, response.getBody().message());
    }

    @Test
    void handleInvalidJson_invalidEnum_returns400() {
        InvalidFormatException invalidFormatEx = mock(InvalidFormatException.class);
        JsonMappingException.Reference reference = mock(JsonMappingException.Reference.class);

        when(reference.getFieldName()).thenReturn("status");
        when(invalidFormatEx.getPath()).thenReturn(List.of(reference));

        doReturn(EventStatus.class).when(invalidFormatEx).getTargetType();

        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
        when(ex.getCause()).thenReturn(invalidFormatEx);

        var response = handler.handleInvalidJson(ex);

        String expectedMessage = "Invalid value for field 'status'. Accepted values: [INACTIVE, ACTIVE, FINISHED]";
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(expectedMessage, response.getBody().message());
    }


    @Test
    void handleInvalidJson_generic_returns400() {
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
        when(ex.getCause()).thenReturn(null);

        var response = handler.handleInvalidJson(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(INVALID_REQUEST_BODY, response.getBody().message());
    }
}