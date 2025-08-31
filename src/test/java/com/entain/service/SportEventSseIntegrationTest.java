package com.entain.service;

import com.entain.data.EventStatus;
import com.entain.data.SportEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SportEventSseIntegrationTest {

    private static final String INTEGRATION_MATCH = "Integration match";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SseEmitterService sseEmitterService;

    @Test
    void subscribeAndReceiveEvent() throws Exception {

        MvcResult result = mockMvc.perform(get("/events/subscribe")
                        .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andReturn();

        SportEvent event = new SportEvent(
                UUID.randomUUID(),
                INTEGRATION_MATCH,
                "Football",
                EventStatus.INACTIVE,
                null
        );
        sseEmitterService.emitUpdate(event);

        String responseContent = result.getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertThat(responseContent).contains("update");
        assertThat(responseContent).contains(INTEGRATION_MATCH);
    }
}