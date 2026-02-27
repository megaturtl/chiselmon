package cc.turtl.chiselmon.command;

import cc.turtl.chiselmon.system.tracker.TrackerManager;
import cc.turtl.chiselmon.system.tracker.TrackerSession;
import cc.turtl.chiselmon.util.MessageUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import java.io.IOException;

public class DashCommand implements ChiselmonCommand {

    @Override
    public String getName() {
        return "dash";
    }

    @Override
    public String getDescription() {
        return "View detailed spawning stats in the Chiselmon Dash.";
    }

    @Override
    public <S> LiteralArgumentBuilder<S> build() {
        return LiteralArgumentBuilder.<S>literal(getName())
                .executes(this::showHelp)
                .then(LiteralArgumentBuilder.<S>literal("open")
                        .executes(this::executeDashboardOpen))
                .then(LiteralArgumentBuilder.<S>literal("close")
                        .executes(this::executeDashboardClose));
    }

    private <S> int showHelp(CommandContext<S> context) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return 0;

        String root = context.getNodes().getFirst().getNode().getName();

        MessageUtils.sendEmptyLine(player);
        MessageUtils.sendSuccess(player, "Chiselmon Dash - Commands");
        MessageUtils.sendPrefixed(player, "  /" + root + " dash status");
        MessageUtils.sendPrefixed(player, "  /" + root + " dash open");
        MessageUtils.sendPrefixed(player, "  /" + root + " dash close");
        return Command.SINGLE_SUCCESS;
    }

    private <S> int executeDashboardOpen(CommandContext<S> context) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return 0;

        TrackerSession session = requireSession(player);
        if (session == null) return Command.SINGLE_SUCCESS;

        if (session.isDashboardRunning()) {
            MessageUtils.sendWarning(player, "Dashboard is already running at http://localhost:"
                    + session.getDashboardPort() + "/");
            return Command.SINGLE_SUCCESS;
        }

        try {
            session.startDashboard();
            MessageUtils.sendSuccess(player, "Dashboard opened at http://localhost:"
                    + session.getDashboardPort() + "/");
        } catch (IOException e) {
            MessageUtils.sendError(player, context, e);
        }

        return Command.SINGLE_SUCCESS;
    }

    private <S> int executeDashboardClose(CommandContext<S> context) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return 0;

        TrackerSession session = requireSession(player);
        if (session == null) return Command.SINGLE_SUCCESS;

        if (!session.isDashboardRunning()) {
            MessageUtils.sendWarning(player, "Dashboard is not running.");
            return Command.SINGLE_SUCCESS;
        }

        session.stopDashboard();
        MessageUtils.sendSuccess(player, "Dashboard closed.");
        return Command.SINGLE_SUCCESS;
    }

    /**
     * Validates a session exists, sending a warning if not. Returns null if invalid.
     */
    private TrackerSession requireSession(LocalPlayer player) {
        try {
            return TrackerManager.getInstance().getTracker();
        } catch (IllegalStateException e) {
            MessageUtils.sendWarning(player, "No active tracker session.");
            return null;
        }
    }
}