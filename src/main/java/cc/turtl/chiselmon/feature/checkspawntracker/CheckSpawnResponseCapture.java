package cc.turtl.chiselmon.feature.checkspawntracker;

import cc.turtl.chiselmon.Chiselmon;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

class CheckSpawnResponseCapture {
    private static final long CAPTURE_TIMEOUT_MS = 1500L;
    private static final Logger LOGGER = Chiselmon.getLogger();
    private final Consumer<List<String>> completionCallback;
    private final List<String> bufferedLines = new ArrayList<>();
    private boolean active;
    private long deadlineMs;

    CheckSpawnResponseCapture(Consumer<List<String>> completionCallback) {
        this.completionCallback = completionCallback;
    }

    boolean begin() {
        if (active) {
            return false;
        }

        bufferedLines.clear();
        active = true;
        deadlineMs = System.currentTimeMillis() + CAPTURE_TIMEOUT_MS;
        return true;
    }

    void cancel() {
        if (!active) {
            return;
        }

        active = false;
        bufferedLines.clear();
        completionCallback.accept(Collections.emptyList());
    }

    boolean isActive() {
        return active;
    }

    void tick() {
        if (!active) {
            return;
        }

        if (System.currentTimeMillis() >= deadlineMs) {
            finish();
        }
    }

    boolean tryCapture(String message) {
        if (!active) {
            return false;
        }

        LOGGER.debug("Attempting to capture message:");
        LOGGER.debug(message);

        if (!matchesResponse(message)) {
            return false;
        }

        bufferedLines.add(message);
        LOGGER.debug("Added message to buffered lines.");

        if (isTerminal(message)) {
            finish();
        }

        return true;
    }

    private void finish() {
        if (!active) {
            return;
        }

        active = false;
        completionCallback.accept(List.copyOf(bufferedLines));
        bufferedLines.clear();
    }

    private boolean matchesResponse(String message) {
        if (message == null || message.isBlank()) {
            return false;
        }

        String normalized = message.toLowerCase(Locale.ROOT);
        return message.contains("%") || normalized.contains("spawn");
    }

    private boolean isTerminal(String message) {
        String normalized = message.toLowerCase(Locale.ROOT);
        return message.contains("%") || normalized.contains("nothing");
    }
}
