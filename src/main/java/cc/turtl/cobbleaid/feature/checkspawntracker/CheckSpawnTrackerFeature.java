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
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
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
    protected boolean isFeatureEnabled() {
        return getConfig().checkSpawnTracker.enabled;
    }

    @Override
    protected void init() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
        HudRenderCallback.EVENT.register(this::onHudRender);

        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            if (!canRun() || overlay) {
                return true;
            }
            boolean captured = this.captureChat(message);
            // if we capture the checkspawn message, don't show it to the user
            return !captured;
        });
    }

    public boolean captureChat(Component component) {
        if (!canRun() || component == null) {
            return false;
        }
        return capture.tryCapture(component.getString());
    }

    private void onClientTick(Minecraft client) {
        capture.tick();

        if (shouldSkipTick(client) || capture.isActive()) {
            return;
        }

        ticksSinceLastPoll++;
        CheckSpawnTrackerConfig config = getConfig().checkSpawnTracker;

        if (ticksSinceLastPoll < config.pollTickInterval) {
            return;
        }

        sendCheckSpawnCommand(client, config);
    }

    private boolean shouldSkipTick(Minecraft client) {
        return !canRun()
                || client == null
                || client.player == null
                || client.screen != null
                || client.isPaused();
    }

    private void sendCheckSpawnCommand(Minecraft client, CheckSpawnTrackerConfig config) {
        ClientPacketListener connection = client.getConnection();
        if (connection == null || !capture.begin()) {
            return;
        }

        try {
            connection.sendCommand("checkspawn " + config.bucket.getId());
            ticksSinceLastPoll = 0;
            CobbleAid.getLogger().debug("Checkspawn command sent silently");
        } catch (Exception e) {
            capture.cancel();
            CobbleAid.getLogger().debug("Checkspawn command failed to send");
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