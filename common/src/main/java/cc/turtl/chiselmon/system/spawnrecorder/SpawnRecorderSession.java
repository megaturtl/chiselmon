package cc.turtl.chiselmon.system.spawnrecorder;

import cc.turtl.chiselmon.system.tracker.TrackerSession;
import cc.turtl.chiselmon.util.format.ColorUtils;
import cc.turtl.chiselmon.util.format.StringFormats;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SpawnRecorderSession {

    private final TrackerSession tracker;
    // Species counts for spawns that occurred while this session was active and unpaused
    private final Map<String, Integer> speciesCounts = new HashMap<>();
    private final long startTimeMs;
    private long accumulatedTimeMs = 0;
    private long lastResumeTime;
    private boolean paused = false;

    public SpawnRecorderSession(TrackerSession tracker) {
        this.startTimeMs = System.currentTimeMillis();
        this.tracker = tracker;
        this.lastResumeTime = System.currentTimeMillis();
    }

    public void onPokemonLoaded(String speciesName) {
        if (paused) return;
        speciesCounts.merge(speciesName, 1, Integer::sum);
    }

    public void tick() {
        MutableComponent message = Component.translatable("chiselmon.spawnrecorder.action_bar.title").withColor(ColorUtils.AQUA.getRGB());
        message.append(Component.literal(String.valueOf(getTotalRecordedCount())).withColor(ColorUtils.CORAL.getRGB()));
        message.append(Component.translatable("chiselmon.spawnrecorder.action_bar.spawns").withColor(ColorUtils.CORAL.getRGB()));
        message.append(Component.literal(StringFormats.formatDurationMs(getElapsedMs())).withColor(ColorUtils.GREEN.getRGB()));
        message.append(Component.translatable("chiselmon.spawnrecorder.action_bar.elapsed").withColor(ColorUtils.GREEN.getRGB()));

        if (isPaused()) {
            message.append(Component.translatable("chiselmon.spawnrecorder.action_bar.paused").withColor(ColorUtils.YELLOW.getRGB()));
        }

        setActionBarStatus(message);
    }

    private static void setActionBarStatus(Component message) {
        var gui = Minecraft.getInstance().gui;
        Minecraft.getInstance().gui.setOverlayMessage(message, false);
    }

    public void pause() {
        if (!paused) {
            accumulatedTimeMs += System.currentTimeMillis() - lastResumeTime;
            paused = true;
        }
    }

    public void resume() {
        if (paused) {
            lastResumeTime = System.currentTimeMillis();
            paused = false;
        }
    }

    public boolean isPaused() {
        return paused;
    }

    public long getStartTimeMs() {
        return startTimeMs;
    }

    public long getElapsedMs() {
        long currentChunk = paused ? 0 : (System.currentTimeMillis() - lastResumeTime);
        return accumulatedTimeMs + currentChunk;
    }

    public int getCurrentlyLoadedCount() {
        return tracker.getCurrentlyLoaded().size();
    }

    public int getTotalRecordedCount() {
        return speciesCounts.values().stream().mapToInt(Integer::intValue).sum();
    }

    public List<Map.Entry<String, Integer>> getTopSpecies(int limit) {
        return speciesCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
}