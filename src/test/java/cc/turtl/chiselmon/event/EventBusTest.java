package cc.turtl.chiselmon.event;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the EventBus.
 * 
 * <p>These tests demonstrate how the event bus enables decoupled
 * communication between features without direct dependencies.
 */
public class EventBusTest {

    // Test event for unit testing
    record TestEvent(String message) implements ChiselmonEvent {}

    @BeforeEach
    void setUp() {
        EventBus.clearAll();
    }

    @Test
    void subscriberReceivesPublishedEvent() {
        List<String> received = new ArrayList<>();
        
        EventBus.subscribe(TestEvent.class, event -> received.add(event.message()));
        EventBus.publish(new TestEvent("hello"));
        
        assertEquals(1, received.size());
        assertEquals("hello", received.get(0));
    }

    @Test
    void multipleSubscribersReceiveEvent() {
        List<String> received1 = new ArrayList<>();
        List<String> received2 = new ArrayList<>();
        
        EventBus.subscribe(TestEvent.class, event -> received1.add(event.message()));
        EventBus.subscribe(TestEvent.class, event -> received2.add(event.message()));
        EventBus.publish(new TestEvent("broadcast"));
        
        assertEquals(1, received1.size());
        assertEquals(1, received2.size());
    }

    @Test
    void subscriberDoesNotReceiveOtherEventTypes() {
        List<UUID> received = new ArrayList<>();
        
        EventBus.subscribe(AlertMutedEvent.class, event -> received.add(event.entityUuid()));
        EventBus.publish(new TestEvent("wrong type"));
        
        assertTrue(received.isEmpty());
    }

    @Test
    void clearListenersRemovesSubscribers() {
        List<String> received = new ArrayList<>();
        
        EventBus.subscribe(TestEvent.class, event -> received.add(event.message()));
        EventBus.clearListeners(TestEvent.class);
        EventBus.publish(new TestEvent("should not receive"));
        
        assertTrue(received.isEmpty());
    }

    @Test
    void clearAllRemovesAllSubscribers() {
        List<String> received1 = new ArrayList<>();
        List<UUID> received2 = new ArrayList<>();
        
        EventBus.subscribe(TestEvent.class, event -> received1.add(event.message()));
        EventBus.subscribe(AlertMutedEvent.class, event -> received2.add(event.entityUuid()));
        EventBus.clearAll();
        
        EventBus.publish(new TestEvent("test"));
        EventBus.publish(new AlertMutedEvent(UUID.randomUUID()));
        
        assertTrue(received1.isEmpty());
        assertTrue(received2.isEmpty());
    }

    @Test
    void publishWithNoSubscribersDoesNotThrow() {
        // Should not throw even with no subscribers
        assertDoesNotThrow(() -> EventBus.publish(new TestEvent("no listeners")));
    }
}
