package cc.turtl.chiselmon.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A simple event bus for internal mod communication.
 * Allows features to publish and subscribe to events without direct coupling.
 * 
 * <p>This follows the Observer pattern and enables loose coupling between
 * features. For example, the SpawnAlert feature can publish a PokemonTrackedEvent
 * that other features can listen to without SpawnAlert knowing about them.
 * 
 * <p>Thread-safe for concurrent subscription and publishing.
 * 
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * // Subscribe to events
 * EventBus.subscribe(PokemonTrackedEvent.class, event -> {
 *     System.out.println("Pokemon tracked: " + event.pokemon().getDisplayName());
 * });
 * 
 * // Publish events
 * EventBus.publish(new PokemonTrackedEvent(pokemonEntity, priority));
 * }</pre>
 */
public final class EventBus {
    private static final Logger LOGGER = LogManager.getLogger("chiselmon");
    private static final Map<Class<? extends ChiselmonEvent>, List<Consumer<? extends ChiselmonEvent>>> LISTENERS = new ConcurrentHashMap<>();

    private EventBus() {
    }

    /**
     * Subscribe to a specific event type.
     *
     * @param <T>       the event type
     * @param eventType the class of the event to subscribe to
     * @param listener  the callback to invoke when the event is published
     */
    public static <T extends ChiselmonEvent> void subscribe(Class<T> eventType, Consumer<T> listener) {
        LISTENERS.computeIfAbsent(eventType, k -> new ArrayList<>()).add(listener);
        LOGGER.debug("EventBus: Subscribed to {}", eventType.getSimpleName());
    }

    /**
     * Publish an event to all registered listeners.
     *
     * @param <T>   the event type
     * @param event the event to publish
     */
    @SuppressWarnings("unchecked")
    public static <T extends ChiselmonEvent> void publish(T event) {
        List<Consumer<? extends ChiselmonEvent>> listeners = LISTENERS.get(event.getClass());
        if (listeners == null || listeners.isEmpty()) {
            return;
        }

        LOGGER.debug("EventBus: Publishing {} to {} listeners",
                event.getClass().getSimpleName(), listeners.size());

        for (Consumer<? extends ChiselmonEvent> listener : listeners) {
            try {
                ((Consumer<T>) listener).accept(event);
            } catch (Exception e) {
                LOGGER.error("EventBus: Error dispatching event {}: {}",
                        event.getClass().getSimpleName(), e.getMessage());
            }
        }
    }

    /**
     * Unsubscribe all listeners for a specific event type.
     * Useful for cleanup during feature shutdown.
     *
     * @param eventType the class of the event to clear listeners for
     */
    public static void clearListeners(Class<? extends ChiselmonEvent> eventType) {
        LISTENERS.remove(eventType);
    }

    /**
     * Clear all event subscriptions. Use with caution - typically only during mod shutdown.
     */
    public static void clearAll() {
        LISTENERS.clear();
    }
}
