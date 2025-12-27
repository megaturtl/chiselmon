package cc.turtl.cobbleaid.feature.checkspawntracker;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.feature.AbstractFeature;
import cc.turtl.cobbleaid.util.ColorUtil;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;

public final class CheckSpawnTrackerFeature extends AbstractFeature {
    public static final CheckSpawnTrackerFeature INSTANCE = new CheckSpawnTrackerFeature();

    private static final int MAX_DISPLAY_ENTRIES = 3;
    private static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("0.##");

    private final CheckSpawnResponseCapture capture;
    private List<CheckSpawnEntry> visibleEntries = List.of();
    private int ticksSinceLastPoll = 0;

    private CheckSpawnTrackerFeature() {
        super("CheckSpawnTracker");
        this.capture = new CheckSpawnResponseCapture(this::updateEntries);
    }

    public static CheckSpawnTrackerFeature getInstance() {
        return INSTANCE;
    }

    @Override
    protected void init() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
        HudRenderCallback.EVENT.register(this::onHudRender);
    }

    @Override
    protected boolean isFeatureEnabled() {
        return getConfig().checkSpawnTracker.enabled;
    }

    public boolean captureChat(Component component) {
        if (!canRun() || component == null) {
            return false;
        }
        return capture.tryCapture(component.getString());
    }

    private void onClientTick(Minecraft client) {
        // Essential tick logic for the capture state machine
        capture.tick();

        if (!canRun()) {
            return;
        }

        if (client == null || client.player == null || client.screen instanceof ChatScreen || client.isPaused()) {
            return;
        }

        if (capture.isActive()) {
            return;
        }

        ticksSinceLastPoll++;
        CheckSpawnTrackerConfig config = getConfig().checkSpawnTracker;

        if (ticksSinceLastPoll < config.pollTickInterval) {
            return;
        }

        ClientPacketListener connection = client.getConnection();
        if (connection != null && capture.begin()) {
            String bucket = (config.bucket == null || config.bucket.isBlank()) ? "common" : config.bucket;
            try {
                connection.sendCommand("checkspawn " + bucket);
                ticksSinceLastPoll = 0;
                CobbleAid.getLogger().debug("Checkspawn command sent silently");
            } catch (Exception e) {
                capture.cancel();
                CobbleAid.getLogger().debug("Checkspawn command failed to send");
            }
        }
    }

    private void onHudRender(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (!canRun() || visibleEntries.isEmpty()) {
            return;
        }

        var font = Minecraft.getInstance().font;
        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();
        int lineHeight = font.lineHeight + 2;
        int startY = (screenHeight / 2) + 10;

        for (int i = 0; i < visibleEntries.size(); i++) {
            CheckSpawnEntry entry = visibleEntries.get(i);
            String text = entry.name() + " " + PERCENT_FORMAT.format(entry.percentage()) + "%";

            int textWidth = font.width(text);
            int x = (screenWidth - textWidth) / 2;
            int y = startY + (i * lineHeight);

            guiGraphics.drawString(font, text, x, y, colorFor(entry.percentage()), true);
        }
    }

    private void updateEntries(List<String> lines) {
        CheckSpawnTrackerConfig config = getConfig().checkSpawnTracker;
        List<CheckSpawnEntry> parsed = CheckSpawnResponseParser.parse(lines);

        if (config.trackedPokemon != null && !config.trackedPokemon.isEmpty()) {
            Set<String> trackedLower = config.trackedPokemon.stream()
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
        if (percentage < 0.01F)
            return ColorUtil.PURPLE;
        if (percentage < 0.1F)
            return ColorUtil.RED;
        if (percentage < 5F)
            return ColorUtil.YELLOW;
        return ColorUtil.GREEN;
    }
}