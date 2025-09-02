package com.entain.service;

import com.entain.data.SportEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
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

    @Test
    void createEmitter_shouldAddEmitter() {

        SseEmitter emitter = service.createEmitter();

        assertThat(emitter).isNotNull();
        assertThat(service.getEmitters()).hasSize(1);
    }

    @Test
    void emitUpdate_shouldSendEventToAllEmitters() throws Exception {
        // given
        SseEmitter emitter = spy(new SseEmitter(0L));
        service.getEmitters().add(emitter);
        // when
        SportEvent event = new SportEvent();
        event.setId(UUID.randomUUID());
        event.setName(TEST_MATCH);
        event.setSport(FOOTBALL);
        // then
        service.emitUpdate(event);

        verify(emitter, atLeastOnce()).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    void emitUpdate_shouldRemoveEmitterIfSendFails() throws Exception {
        // given
        SseEmitter badEmitter = mock(SseEmitter.class);
        doThrow(new IOException("fail")).when(badEmitter).send(any(SseEmitter.SseEventBuilder.class));

        service.getEmitters().add(badEmitter);

        SportEvent event = new SportEvent();
        event.setId(UUID.randomUUID());
        event.setName(BROKEN_MATCH);
        event.setSport(BASKETBALL);
        // when
        service.emitUpdate(event);
        // then
        assertThat(service.getEmitters()).isEmpty();
    }

}