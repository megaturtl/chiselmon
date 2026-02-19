package cc.turtl.chiselmon.command;

import cc.turtl.chiselmon.system.spawnrecorder.SpawnRecorderManager;
import cc.turtl.chiselmon.system.spawnrecorder.SpawnRecorderSession;
import cc.turtl.chiselmon.util.MessageUtils;
import cc.turtl.chiselmon.util.format.StringFormats;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

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
        return 1;
    }

    private int executePause(CommandContext<CommandSourceStack> context) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return 0;

        SpawnRecorderSession session = SpawnRecorderManager.getInstance().getSession();

        if (session == null) {
            MessageUtils.sendWarning(player, "No active session to pause.");
            return 1;
        }
        if (session.isPaused()) {
            MessageUtils.sendWarning(player, "Session is already paused.");
            return 1;
        }
        session.pause();
        MessageUtils.sendSuccess(player, "Spawn Recorder paused.");
        return 1;
    }

    private int executeResume(CommandContext<CommandSourceStack> context) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return 0;

        SpawnRecorderSession session = SpawnRecorderManager.getInstance().getSession();

        if (session == null) {
            MessageUtils.sendWarning(player, "No active session to resume.");
            return 1;
        }
        if (!session.isPaused()) {
            MessageUtils.sendWarning(player, "Session is already running.");
            return 1;
        }
        session.resume();
        MessageUtils.sendSuccess(player, "Spawn Recorder resumed.");
        return 1;
    }

    private int executeStop(CommandContext<CommandSourceStack> context) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return 0;

        SpawnRecorderSession finished = SpawnRecorderManager.getInstance().stopSession();

        if (finished == null) {
            MessageUtils.sendWarning(player, "No active session to stop.");
            return 1;
        }
        MessageUtils.sendSuccess(player, "Spawn Recorder - Session Ended");
        MessageUtils.sendLabeled(player, " Spawns recorded:", finished.getTotalRecordedCount());
        MessageUtils.sendLabeled(player, " Time elapsed:", StringFormats.formatDurationMs(finished.getElapsedMs()));
        return 1;
    }

    private int executeSummary(CommandContext<CommandSourceStack> context) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return 0;

        SpawnRecorderSession session = SpawnRecorderManager.getInstance().getSession();

        if (session == null) {
            MessageUtils.sendWarning(player, "No active session.");
            return 1;
        }
        List<Map.Entry<String, Integer>> top = session.getTopSpecies(10);
        if (top.isEmpty()) {
            MessageUtils.sendWarning(player, "No spawns recorded yet.");
            return 1;
        }
        MessageUtils.sendSuccess(player, "Spawn Recorder - Top Species (" + session.getTotalRecordedCount() + " total recorded)");
        int rank = 1;
        for (Map.Entry<String, Integer> entry : top) {
            MessageUtils.sendLabeled(player, rank + ". ", entry.getKey() + " - " + entry.getValue());
            rank++;
        }
        return 1;
    }

}