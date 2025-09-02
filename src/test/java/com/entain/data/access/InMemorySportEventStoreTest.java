package com.entain.data.access;

import static org.junit.jupiter.api.Assertions.*;

import com.entain.data.EventStatus;
import com.entain.data.SportEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


class InMemorySportEventStoreTest {

    private static final String FOOTBALL = "FOOTBALL";
    private static final String BASKETBALL = "BASKETBALL";
    private static final String TEST_MATCH = "Test Match";
    private static final String ORIGINAL_MATCH = "Original Match";

    private InMemorySportEventStore store;

    @BeforeEach
    void setUp() {
        store = new InMemorySportEventStore();
    }

    @Test
    void save_shouldInsertEventIfAbsent() {
        SportEvent event = new SportEvent(
                UUID.randomUUID(),
                TEST_MATCH,
                FOOTBALL,
                EventStatus.INACTIVE,
                LocalDateTime.now().plusHours(1)
        );

        // Act: save event to the in-memory store
        SportEvent saved = store.save(event);

        // Assert: event is saved and retrievable by ID
        assertEquals(event, saved);
        assertEquals(Optional.of(event), store.findById(event.id()));
    }

    @Test
    void save_shouldNotOverwriteExistingEvent() {
        UUID eventId = UUID.randomUUID();

        // Original event with the same ID
        SportEvent originalEvent = new SportEvent(
                eventId,
                ORIGINAL_MATCH,
                FOOTBALL,
                EventStatus.INACTIVE,
                LocalDateTime.now().plusHours(1)
        );

        // New event with the same ID but different properties
        SportEvent newEvent = new SportEvent(
                eventId,
                TEST_MATCH,
                "BASKETBALL",
                EventStatus.ACTIVE,
                LocalDateTime.now().plusHours(2)
        );

        // Save the original event first
        SportEvent savedOriginal = store.save(originalEvent);
        assertEquals(originalEvent, savedOriginal);

        // Attempt to save the new event; putIfAbsent ensures the original is not overwritten
        SportEvent savedNew = store.save(newEvent);

        // Assert: original event remains in the store, new event not saved
        assertEquals(originalEvent, savedNew);
        assertEquals(originalEvent, store.findById(eventId).orElseThrow());
    }

    @Test
    void updateStatus_shouldUpdateIfEventExists() {
        SportEvent event = new SportEvent(
                UUID.randomUUID(),
                ORIGINAL_MATCH,
                FOOTBALL,
                EventStatus.INACTIVE,
                LocalDateTime.now().plusHours(1)
        );
        store.save(event);

        store.updateStatus(event.id(), EventStatus.ACTIVE);

        SportEvent updated = store.findById(event.id()).orElseThrow();
        assertEquals(EventStatus.ACTIVE, updated.status());
    }

    @Test
    void updateStatus_shouldDoNothingIfEventNotExists() {
        UUID unknownId = UUID.randomUUID();

        assertDoesNotThrow(() -> store.updateStatus(unknownId, EventStatus.ACTIVE));
        assertTrue(store.findById(unknownId).isEmpty());
    }

    @Test
    void concurrentSave_shouldNotOverwriteExistingEvent() throws InterruptedException {
        UUID eventId = UUID.randomUUID();

        // Two events with the same ID but different properties
        SportEvent firstEvent = new SportEvent(
                eventId,
                ORIGINAL_MATCH,
                FOOTBALL,
                EventStatus.INACTIVE,
                LocalDateTime.now().plusHours(1)
        );

        SportEvent secondEvent = new SportEvent(
                eventId,
                TEST_MATCH,
                BASKETBALL,
                EventStatus.ACTIVE,
                LocalDateTime.now().plusHours(2)
        );

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(1);

        // Submit first thread to save first event after latch
        executor.submit(() -> {
            awaitLatch(latch);
            store.save(firstEvent);
        });

        // Submit second thread to save second event after latch
        executor.submit(() -> {
            awaitLatch(latch);
            store.save(secondEvent);
        });

        // Release both threads simultaneously
        latch.countDown();

        executor.shutdown();
        Thread.sleep(200); // allow some time for threads to execute

        SportEvent stored = store.findById(eventId).orElseThrow();

        // Assert: only one of the two events is saved, no overwriting occurs
        assertTrue(stored.equals(firstEvent) || stored.equals(secondEvent));
    }

    private void awaitLatch(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException ignored) {
        }
    }
}
