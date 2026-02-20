package cc.turtl.chiselmon.system.spawnrecorder;

import cc.turtl.chiselmon.config.ChiselmonConfig;
import cc.turtl.chiselmon.system.tracker.TrackerSession;
import cc.turtl.chiselmon.util.format.ColorUtils;
import cc.turtl.chiselmon.util.format.StringFormats;
import cc.turtl.chiselmon.util.render.PokemonEntityUtils;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class SpawnRecorderSession {
    private static final int DESPAWN_MIN_TICKS = 600;
    private static final int EXPIRY_MS = 5 * 60 * 1000; // remove tickdata after 5min
    private final TrackerSession tracker;
    // Species counts for spawns that occurred while this session was active and unpaused
    private final Map<String, Integer> speciesCounts = new HashMap<>();
    private final long startTimeMs;
    private final Map<UUID, TickData> tickAges = new HashMap<>();
    private long accumulatedTimeMs = 0;
    private long lastResumeTime;
    private boolean paused = false;

    public SpawnRecorderSession(TrackerSession tracker) {
        this.startTimeMs = System.currentTimeMillis();
        this.tracker = tracker;
        this.lastResumeTime = System.currentTimeMillis();
    }

    private void setActionBarStatus() {
        MutableComponent message = Component.translatable("chiselmon.spawnrecorder.action_bar.title").withColor(ColorUtils.PINK.getRGB());
        message.append(Component.literal(String.valueOf(getCurrentlyLoadedCount())).withColor(ColorUtils.AQUA.getRGB()));
        message.append(Component.literal("/").withColor(ColorUtils.LIGHT_GRAY.getRGB()));
        message.append(Component.literal(String.valueOf(getTotalRecordedCount())).withColor(ColorUtils.TEAL.getRGB()));
        message.append(Component.translatable("chiselmon.spawnrecorder.action_bar.spawns").withColor(ColorUtils.AQUA.getRGB()));
        message.append(Component.literal(StringFormats.formatDurationMs(getElapsedMs())).withColor(ColorUtils.GREEN.getRGB()));

        if (isPaused()) {
            message.append(Component.translatable("chiselmon.spawnrecorder.action_bar.paused").withColor(ColorUtils.YELLOW.getRGB()));
        }

        Minecraft.getInstance().gui.setOverlayMessage(message, false);
    }

    public void onPokemonLoaded(PokemonEntity entity) {
        if (paused) return;
        if (tickAges.containsKey(entity.getUUID())) return; // don't double count
        speciesCounts.merge(entity.getPokemon().getSpecies().getName(), 1, Integer::sum);
        tickAges.computeIfAbsent(entity.getUUID(), k -> new TickData());
    }

    public void onPokemonUnloaded(PokemonEntity entity) {
        TickData tickData = tickAges.get(entity.getUUID());
        if (tickData != null) {
            tickData.accumulatedTicks += entity.getTicksLived();
            tickData.lastSeenMs = getElapsedMs();
        }
    }

    public void tick() {
        removeOldTickData();

        if (ChiselmonConfig.get().recorder.actionBar) {
            setActionBarStatus();
        }

        for (PokemonEntity entity : tracker.getCurrentlyLoaded().values()) {
            if (ChiselmonConfig.get().recorder.despawnGlow) {
                int rgb = ((getTicksLived(entity)) >= DESPAWN_MIN_TICKS) ? ColorUtils.RED.getRGB() : ColorUtils.LIME.getRGB();
                PokemonEntityUtils.addGlow(entity, rgb);
                PokemonEntityUtils.highlightNickname(entity, rgb);
            }
        }
    }

    private void removeOldTickData() {
        // Remove UUIDs that haven't been seen lately
        tickAges.entrySet().removeIf(e -> getElapsedMs() - e.getValue().lastSeenMs >= EXPIRY_MS);
    }

    public int getTicksLived(PokemonEntity entity) {
        UUID uuid = entity.getUUID();
        TickData tickData = tickAges.get(uuid);
        int accumulated = (tickData != null) ? tickData.accumulatedTicks : 0;
        return accumulated + entity.getTicksLived();
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

    public float getSpawnsPerMinute() {
        return (float) getTotalRecordedCount() / ((float) getElapsedMs() / 60_000);
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

    /**
     * Data for keeping track of the real tick age of a pokemon entity
     */
    private static class TickData {
        int accumulatedTicks;
        long lastSeenMs;
    }
}