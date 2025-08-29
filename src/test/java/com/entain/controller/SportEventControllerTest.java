package com.entain.controller;

import com.entain.data.EventStatus;
import com.entain.data.SportEvent;
import com.entain.dto.CreateEventRequest;
import com.entain.dto.UpdateStatusRequest;
import com.entain.exception.EventNotFoundException;
import com.entain.exception.GlobalExceptionHandler;
import com.entain.service.SportEventService;
import com.entain.service.SseEmitterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.entain.config.EntainConstant.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SportEventControllerTest {

    private static final String FOOTBALL = "FOOTBALL";
    private static final String LEAGUE_FINAL = "Champions League Final";
    private static final String INACTIVE = "INACTIVE";

    private MockMvc mockMvc;

    @Mock
    private SportEventService service;

    @Mock
    private SseEmitterService sseEmitterService;

    @InjectMocks
    private SportEventController controller;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private UUID eventId;
    private SportEvent event;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        eventId = UUID.randomUUID();
        event = new SportEvent(
                eventId,
                LEAGUE_FINAL,
                FOOTBALL,
                EventStatus.INACTIVE,
                LocalDateTime.of(2025, 9, 1, 20, 0)
        );
    }

    @Test
    void createEvent_success() throws Exception {
        CreateEventRequest request = new CreateEventRequest("Final Match", FOOTBALL, event.getStartTime());
        SportEvent createdEvent = new SportEvent(eventId, request.getName(), request.getSport(), EventStatus.INACTIVE, request.getStartTime());

        when(service.createEvent(any(SportEvent.class))).thenReturn(createdEvent);

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventId.toString()))
                .andExpect(jsonPath("$.name").value("Final Match"))
                .andExpect(jsonPath("$.sport").value("FOOTBALL"))
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    void getEvents_returnsList() throws Exception {
        when(service.getEvents(EventStatus.INACTIVE, "FOOTBALL"))
                .thenReturn(List.of(event));

        mockMvc.perform(get("/events")
                        .param("status", INACTIVE)
                        .param("sport", FOOTBALL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(eventId.toString()))
                .andExpect(jsonPath("$[0].status").value(INACTIVE));
    }

    @Test
    void getEvents_noFilters_returnsAllEvents() throws Exception {
        SportEvent anotherEvent = new SportEvent(
                UUID.randomUUID(),
                "World Cup Final",
                FOOTBALL,
                EventStatus.ACTIVE,
                LocalDateTime.of(2025, 12, 15, 18, 0)
        );

        when(service.getEvents(null, null))
                .thenReturn(List.of(event, anotherEvent));

        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(event.getId().toString()))
                .andExpect(jsonPath("$[1].id").value(anotherEvent.getId().toString()));
    }

    @Test
    void changeStatus_success() throws Exception {
        UpdateStatusRequest request = new UpdateStatusRequest(EventStatus.ACTIVE);
        SportEvent updatedEvent = new SportEvent(
                eventId,
                event.getName(),
                event.getSport(),
                EventStatus.ACTIVE,
                event.getStartTime()
        );

        when(service.changeStatus(eq(eventId), eq(EventStatus.ACTIVE)))
                .thenReturn(updatedEvent);

        mockMvc.perform(patch("/events/{id}/status", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventId.toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void changeStatus_eventNotFound_returnsNotFound() throws Exception {
        UpdateStatusRequest request = new UpdateStatusRequest(EventStatus.ACTIVE);

        when(service.changeStatus(eq(eventId), eq(EventStatus.ACTIVE)))
                .thenThrow(new EventNotFoundException(eventId));

        mockMvc.perform(patch("/events/{id}/status", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(SPORT_EVENT + eventId + NOT_FOUND));
    }

    @Test
    void changeStatus_invalidEnumValue_returnsBadRequest() throws Exception {
        String invalidJson = """
                {"status":"ACTIVE1"}
                """;

        mockMvc.perform(patch("/events/{id}/status", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid value for field 'status'. Accepted values: [INACTIVE, ACTIVE, FINISHED]"));
    }

    @Test
    void changeStatus_emptyBody_returnsBadRequest() throws Exception {
        mockMvc.perform(patch("/events/{id}/status", eventId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(INVALID_REQUEST_BODY));
    }

    @Test
    void subscribe_returnsEmitter() throws Exception {
        when(sseEmitterService.createEmitter()).thenReturn(new SseEmitter());

        mockMvc.perform(get("/events/subscribe"))
                .andExpect(status().isOk());
    }

}