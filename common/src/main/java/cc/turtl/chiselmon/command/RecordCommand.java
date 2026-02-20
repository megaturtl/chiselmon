package cc.turtl.chiselmon.command;

import cc.turtl.chiselmon.system.spawnrecorder.SpawnRecorderManager;
import cc.turtl.chiselmon.system.spawnrecorder.SpawnRecorderSession;
import cc.turtl.chiselmon.util.MessageUtils;
import cc.turtl.chiselmon.util.format.ColorUtils;
import cc.turtl.chiselmon.util.format.StringFormats;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Map;


public class RecordCommand implements ChiselmonCommand {
    @Override
    public String getName() {
        return "record";
    }

    @Override
    public String getDescription() {
        return "Record pokemon spawn data";
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal(getName())
                .executes(this::showHelp)
                .then(Commands.literal("start")
                        .executes(this::executeStart))
                .then(Commands.literal("pause")
                        .executes(this::executePause))
                .then(Commands.literal("resume")
                        .executes(this::executeResume))
                .then(Commands.literal("stop")
                        .executes(this::executeStop))
                .then(Commands.literal("summary")
                        .executes(this::executeSummary));
    }

    private int showHelp(CommandContext<CommandSourceStack> context) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return 0;

        String root = context.getNodes().getFirst().getNode().getName();

        MessageUtils.sendHeader(player, "Spawn Recorder Commands");
        MessageUtils.sendPrefixed(player, "/" + root + " record start");
        MessageUtils.sendPrefixed(player, "/" + root + " record pause");
        MessageUtils.sendPrefixed(player, "/" + root + " record resume");
        MessageUtils.sendPrefixed(player, "/" + root + " record stop");
        MessageUtils.sendPrefixed(player, "/" + root + " record summary");
        return Command.SINGLE_SUCCESS;
    }

    private int executeStart(CommandContext<CommandSourceStack> context) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return 0;
        try {
            boolean started = SpawnRecorderManager.getInstance().startSession();
            if (!started) {
                MessageUtils.sendWarning(player, "A Spawn Recorder session is already running!");
            } else {
                MessageUtils.sendSuccess(player, "Spawn Recorder session started!");
            }
        } catch (Exception e) {
            MessageUtils.sendError(player, context, e);
        }
        return Command.SINGLE_SUCCESS;
    }

    private int executePause(CommandContext<CommandSourceStack> context) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return 0;

        SpawnRecorderSession session = requireSession(player);
        if (session == null) return Command.SINGLE_SUCCESS;

        if (session.isPaused()) {
            MessageUtils.sendWarning(player, "Session is already paused.");
            return Command.SINGLE_SUCCESS;
        }
        session.pause();
        MessageUtils.sendSuccess(player, "Spawn Recorder paused.");
        return Command.SINGLE_SUCCESS;
    }

    private int executeResume(CommandContext<CommandSourceStack> context) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return 0;

        SpawnRecorderSession session = requireSession(player);
        if (session == null) return Command.SINGLE_SUCCESS;

        if (!session.isPaused()) {
            MessageUtils.sendWarning(player, "Session is already running.");
            return Command.SINGLE_SUCCESS;
        }
        session.resume();
        MessageUtils.sendSuccess(player, "Spawn Recorder resumed.");
        return Command.SINGLE_SUCCESS;
    }

    private int executeStop(CommandContext<CommandSourceStack> context) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return 0;

        SpawnRecorderSession finished = SpawnRecorderManager.getInstance().stopSession();
        if (finished == null) {
            MessageUtils.sendWarning(player, "No active session to stop.");
            return Command.SINGLE_SUCCESS;
        }

        MessageUtils.sendEmptyLine(player);
        sendSessionSummary(player, "Session Ended", finished, 3);
        return Command.SINGLE_SUCCESS;
    }

    private int executeSummary(CommandContext<CommandSourceStack> context) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return 0;

        SpawnRecorderSession session = requireSession(player);
        if (session == null) return Command.SINGLE_SUCCESS;

        if (session.getTopSpecies(1).isEmpty()) {
            MessageUtils.sendWarning(player, "No spawns recorded yet.");
            return Command.SINGLE_SUCCESS;
        }
        MessageUtils.sendEmptyLine(player);
        sendSessionSummary(player, "Session Summary", session, 10);
        return Command.SINGLE_SUCCESS;
    }

    private void sendSessionSummary(LocalPlayer player, String title, SpawnRecorderSession session, int topCount) {
        MessageUtils.sendSuccess(player, "Spawn Recorder - " + title);
        MessageUtils.sendLabeled(player, "  Time elapsed", StringFormats.formatDurationMs(session.getElapsedMs()));
        MessageUtils.sendLabeled(player, "  Spawns", session.getTotalRecordedCount()
                + " (" + StringFormats.formatDecimal(session.getSpawnsPerMinute()) + "/min)");
        sendTopSpecies(player, session.getTopSpecies(topCount));
    }

    private void sendTopSpecies(LocalPlayer player, List<Map.Entry<String, Integer>> top) {
        int rank = 1;
        for (Map.Entry<String, Integer> entry : top) {
            Component line = Component.empty()
                    .append(Component.literal("    #" + rank).withColor(ColorUtils.AQUA.getRGB()))
                    .append(Component.literal(" » ").withColor(ColorUtils.DARK_GRAY.getRGB()))
                    .append(Component.literal(entry.getKey()).withColor(ColorUtils.PINK.getRGB()))
                    .append(Component.literal(" - ").withColor(ColorUtils.DARK_GRAY.getRGB()))
                    .append(Component.literal(entry.getValue() + " spawns").withColor(ColorUtils.WHITE.getRGB()));
            MessageUtils.sendPrefixed(player, line);
            rank++;
        }
    }

    /**
     * Validates a session exists, sending a warning if not. Returns null if invalid.
     */
    private SpawnRecorderSession requireSession(LocalPlayer player) {
        SpawnRecorderSession session = SpawnRecorderManager.getInstance().getSession();
        if (session == null) MessageUtils.sendWarning(player, "No active session.");
        return session;
    }
}