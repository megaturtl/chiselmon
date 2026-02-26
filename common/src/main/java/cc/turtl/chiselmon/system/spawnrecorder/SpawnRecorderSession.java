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
    private static final int TICKS_PER_SECOND = 20;
    private static final int DESPAWN_MIN_TICKS = 600;
    private static final int EXPIRY_TICKS = 5 * 60 * TICKS_PER_SECOND;

    private final TrackerSession tracker;

    // Species counts for spawns that occurred while this session was active and unpaused
    private final Map<String, Integer> speciesCounts = new HashMap<>();
    // Data for keeping track of the real tick age of a pokemon entity
    private final Map<UUID, TickData> tickAges = new HashMap<>();

    private int sessionTicks = 0;
    private boolean paused = false;

    public SpawnRecorderSession(TrackerSession tracker) {
        this.tracker = tracker;
    }

    public void tick() {
        if (!paused) sessionTicks++;
        removeOldTickData();

        if (ChiselmonConfig.get().recorder.actionBar) {
            setActionBarStatus();
        }

        if (ChiselmonConfig.get().recorder.despawnGlow) {
            tracker.getCurrentlyLoaded().values().forEach(entity -> {
                int rgb = getTicksLived(entity) >= DESPAWN_MIN_TICKS ? ColorUtils.RED.getRGB() : ColorUtils.LIME.getRGB();
                PokemonEntityUtils.addGlow(entity, rgb);
                PokemonEntityUtils.highlightNickname(entity, rgb);
            });
        }
    }

    private void removeOldTickData() {
        // Remove UUIDs that haven't been seen lately and are no longer loaded
        tickAges.entrySet().removeIf(e ->
                !tracker.getCurrentlyLoaded().containsKey(e.getKey()) &&
                        sessionTicks - e.getValue().sessionTicksAtLastSeen >= EXPIRY_TICKS
        );
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
            tickData.sessionTicksAtLastSeen = sessionTicks;
        }
    }

    public void pause() {
        paused = true;
    }

    public void resume() {
        paused = false;
    }

    public boolean isPaused() {
        return paused;
    }

    public int getTicksLived(PokemonEntity entity) {
        TickData tickData = tickAges.get(entity.getUUID());
        int accumulated = (tickData != null) ? tickData.accumulatedTicks : 0;
        return accumulated + entity.getTicksLived();
    }

    public long getElapsedMs() {
        return (sessionTicks * 1000L) / TICKS_PER_SECOND;
    }

    public float getSpawnsPerMinute() {
        return (float) getTotalRecordedCount() / ((float) getElapsedMs() / 60_000);
    }

    public int getCurrentlyLoadedCount() {
        return tracker.getCurrentlyLoaded().size();
    }

    public int getDespawnEligibleCount() {
        return (int) tracker.getCurrentlyLoaded().values().stream()
                .filter(entity -> getTicksLived(entity) >= DESPAWN_MIN_TICKS)
                .count();
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

    private void setActionBarStatus() {
        int loadedCount = getCurrentlyLoadedCount();
        int despawnCount = getDespawnEligibleCount();
        int safeCount = loadedCount - despawnCount;

        MutableComponent message = Component.empty()
                .append(Component.translatable("chiselmon.spawnrecorder.action_bar.loaded").withColor(ColorUtils.LIGHT_GRAY.getRGB()))
                .append(Component.literal(String.valueOf(despawnCount)).withColor(ColorUtils.RED.getRGB()))
                .append(Component.literal("/").withColor(ColorUtils.DARK_GRAY.getRGB()))
                .append(Component.literal(String.valueOf(safeCount)).withColor(ColorUtils.GREEN.getRGB()))
                .append(Component.literal(" | ").withColor(ColorUtils.DARK_GRAY.getRGB()))
                .append(Component.translatable("chiselmon.spawnrecorder.action_bar.spawns").withColor(ColorUtils.LIGHT_GRAY.getRGB()))
                .append(Component.literal(String.valueOf(getTotalRecordedCount())).withColor(ColorUtils.AQUA.getRGB()))
                .append(Component.literal(" | ").withColor(ColorUtils.DARK_GRAY.getRGB()))
                .append(Component.literal(StringFormats.formatDurationMs(getElapsedMs())).withColor(ColorUtils.YELLOW.getRGB()));

        if (isPaused()) {
            message.append(Component.translatable("chiselmon.spawnrecorder.action_bar.paused").withColor(ColorUtils.ORANGE.getRGB()));
        }

        Minecraft.getInstance().gui.setOverlayMessage(message, false);
    }

    private static class TickData {
        int accumulatedTicks;
        int sessionTicksAtLastSeen;
    }
}