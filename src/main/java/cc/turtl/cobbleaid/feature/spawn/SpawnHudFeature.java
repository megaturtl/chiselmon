package cc.turtl.cobbleaid.feature.spawn;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.ModConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;

public final class SpawnHudFeature {
    private static final long MIN_POLL_INTERVAL_MS = 3000L;
    private static final int MAX_DISPLAY_ENTRIES = 3;
    private static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("0.##");
    private static final int COLOR_PURPLE = 0xFFAAFF;
    private static final int COLOR_RED = 0xFF5555;
    private static final int COLOR_YELLOW = 0xFFFF55;
    private static final int COLOR_GREEN = 0x55FF55;

    private static SpawnHudFeature INSTANCE;

    private final SpawnResponseCapture capture;
    private List<SpawnEntry> visibleEntries = List.of();
    private long lastPollMs = 0L;

    private SpawnHudFeature() {
        this.capture = new SpawnResponseCapture(this::updateEntries);
    }

    public static void register() {
        INSTANCE = new SpawnHudFeature();
        INSTANCE.registerListeners();
    }

    public static boolean captureChat(Component component) {
        SpawnHudFeature instance = INSTANCE;
        if (instance == null || component == null) {
            return false;
        }

        return instance.capture.tryCapture(component.getString());
    }

    private void registerListeners() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
        HudRenderCallback.EVENT.register(this::onHudRender);
    }

    private void onClientTick(Minecraft client) {
        capture.tick();

        ModConfig config = CobbleAid.services().config().get();

        if (config.modDisabled || !config.spawnTracker.enabled) {
            capture.cancel();
            visibleEntries = List.of();
            return;
        }

        if (client == null || client.player == null) {
            capture.cancel();
            visibleEntries = List.of();
            return;
        }

        if (client.screen instanceof ChatScreen) {
            return;
        }

        if (client.isPaused()) {
            return;
        }

        long now = System.currentTimeMillis();

        if (capture.isActive()) {
            return;
        }

        if (now - lastPollMs < MIN_POLL_INTERVAL_MS) {
            return;
        }

        ClientPacketListener connection = client.getConnection();
        if (connection == null) {
            return;
        }

        if (!capture.begin()) {
            return;
        }

        String bucket = config.spawnTracker.bucket;
        if (bucket == null || bucket.isBlank()) {
            bucket = "common";
        }

        try {
            connection.sendCommand("checkspawn " + bucket);
            lastPollMs = now;
        } catch (Exception ignored) {
            capture.cancel();
        }
    }

    private void onHudRender(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        ModConfig config = CobbleAid.services().config().get();
        if (config.modDisabled || !config.spawnTracker.enabled) {
            return;
        }

        if (visibleEntries.isEmpty()) {
            return;
        }

        int x = config.spawnTracker.hudX;
        int y = config.spawnTracker.hudY;

        var font = Minecraft.getInstance().font;
        int lineHeight = font.lineHeight + 2;

        for (int i = 0; i < visibleEntries.size(); i++) {
            SpawnEntry entry = visibleEntries.get(i);
            String text = entry.name() + " " + PERCENT_FORMAT.format(entry.percentage()) + "%";
            int color = colorFor(entry.percentage());
            guiGraphics.drawString(font, text, x, y + (i * lineHeight), color, true);
        }
    }

    private void updateEntries(List<String> lines) {
        ModConfig config = CobbleAid.services().config().get();
        List<SpawnEntry> parsed = SpawnResponseParser.parse(lines);

        List<String> tracked = config.spawnTracker.trackedPokemon;
        if (tracked != null && !tracked.isEmpty()) {
            Set<String> trackedLower = tracked.stream()
                    .map(name -> name.toLowerCase(Locale.ROOT))
                    .collect(Collectors.toCollection(HashSet::new));

            parsed = parsed.stream()
                    .filter(entry -> trackedLower.contains(entry.name().toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        }

        visibleEntries = parsed.stream()
                .limit(MAX_DISPLAY_ENTRIES)
                .toList();
    }

    private int colorFor(float percentage) {
        if (percentage < 0.01F) {
            return COLOR_PURPLE;
        }

        if (percentage < 0.1F) {
            return COLOR_RED;
        }

        if (percentage < 5F) {
            return COLOR_YELLOW;
        }

        return COLOR_GREEN;
    }
}
