package com.entain.service;

import com.entain.data.EventStatus;
import com.entain.data.SportEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SseEmitterServiceTest {

    private static final String TEST_MATCH = "Test match";
    private static final String FOOTBALL = "Football";
    private static final String BROKEN_MATCH = "Broken match";
    private static final String BASKETBALL = "Basketball";

    private SseEmitterService service;

    @BeforeEach
    void setUp() {
        service = new SseEmitterService();
    }

    private SportEvent createEvent(String name, String sport) {
        return new SportEvent(
                UUID.randomUUID(),
                name,
                sport,
                EventStatus.INACTIVE,
                LocalDateTime.now().plusHours(1)
        );
    }

    @Test
    void createEmitter_shouldAddEmitter() {
        SseEmitter emitter = service.createEmitter();

        assertThat(emitter).isNotNull();
        assertThat(service.getEmitters()).hasSize(1);
    }

    @Test
    void emitUpdate_shouldSendEventToAllEmitters() throws Exception {
        SseEmitter emitter = spy(new SseEmitter(0L));
        service.getEmitters().add(emitter);

        SportEvent event = createEvent(TEST_MATCH, FOOTBALL);

        service.emitUpdate(event);

        verify(emitter, atLeastOnce()).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    void emitUpdate_shouldRemoveEmitterIfSendFails() throws Exception {
        SseEmitter badEmitter = mock(SseEmitter.class);
        doThrow(new IOException("fail"))
                .when(badEmitter)
                .send(any(SseEmitter.SseEventBuilder.class));

        service.getEmitters().add(badEmitter);

        SportEvent event = createEvent(BROKEN_MATCH, BASKETBALL);

        service.emitUpdate(event);

        assertThat(service.getEmitters()).isEmpty();
    }
}