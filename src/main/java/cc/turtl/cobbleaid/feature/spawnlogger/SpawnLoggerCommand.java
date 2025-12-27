package cc.turtl.cobbleaid.feature.spawnlogger;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;
import cc.turtl.cobbleaid.util.CommandUtils;
import cc.turtl.cobbleaid.CobbleAid;

public class SpawnLoggerCommand {
    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {

        var startBranch = literal("start")
                .executes(SpawnLoggerCommand::executeStart);

        var pauseBranch = literal("pause")
                .executes(SpawnLoggerCommand::executePause);
        
        var resumeBranch = literal("resume")
                .executes(SpawnLoggerCommand::executeResume);

        var stopBranch = literal("stop")
                .executes(SpawnLoggerCommand::executeStop);

        return literal("log")
                .executes(SpawnLoggerCommand::executeHelp)
                .then(startBranch)
                .then(pauseBranch)
                .then(resumeBranch)
                .then(stopBranch);
    }

    private static int executeHelp(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        CommandUtils.sendHeader(source, "Spawn Logger Commands");
        CommandUtils.sendUsageWithDescription(source, "/" + CobbleAid.MODID + " log start <minutes>",
                "Starts a Spawn Logger session.");
        CommandUtils.sendUsageWithDescription(source, "/" + CobbleAid.MODID + " log pause",
                "Pauses/resumes the current session.");
        return 1;
    }

    private static int executeStart(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        SpawnLoggerFeature spawnLogger = SpawnLoggerFeature.getInstance();

        if (spawnLogger.getSession() != null) {
            CommandUtils.sendWarning(source, "Spawn Logger already running!");
            return 1;
        }

        try {
            spawnLogger.startSession();
            CommandUtils.sendSuccess(source, "Spawn Logger session started!");
        } catch (Exception e) {
            CommandUtils.sendError(source, "An unexpected error occurred during 'log start' command!");
            CobbleAid.getLogger().error("Error executing 'log start' command:", e);
            return 0;
        }

        return 1;
    }

    private static int executePause(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        SpawnLoggerSession session = SpawnLoggerFeature.getInstance().getSession();

        if (session == null) {
            CommandUtils.sendWarning(source, "No Spawn Logger currently running!");
            return 1;
        }

        if (session.isPaused()) {
            CommandUtils.sendWarning(source, "Spawn Logger is already paused!");
            return 1;
        }

        try {
            session.pause();
            CommandUtils.sendSuccess(source, "Spawn Logger paused.");
        } catch (Exception e) {
            CommandUtils.sendError(source, "An unexpected error occurred during 'log pause' command!");
            CobbleAid.getLogger().error("Error executing 'log pause' command:", e);
            return 0;
        }

        return 1;
    }

    private static int executeResume(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        SpawnLoggerSession session = SpawnLoggerFeature.getInstance().getSession();

        if (session == null) {
            CommandUtils.sendWarning(source, "No Spawn Logger currently running!");
            return 1;
        }

        if (!session.isPaused()) {
            CommandUtils.sendWarning(source, "Spawn Logger is not paused!");
            return 1;
        }

        try {
            session.resume();
            CommandUtils.sendSuccess(source, "Spawn Logger resumed.");
        } catch (Exception e) {
            CommandUtils.sendError(source, "An unexpected error occurred during 'log pause' command!");
            CobbleAid.getLogger().error("Error executing 'log pause' command:", e);
            return 0;
        }

        return 1;
    }

    private static int executeStop(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        SpawnLoggerFeature spawnLogger = SpawnLoggerFeature.getInstance();

        if (spawnLogger.getSession() == null) {
            CommandUtils.sendWarning(source, "No Spawn Logger currently running!");
            return 1;
        }

        try {
            spawnLogger.finishSession();
        } catch (Exception e) {
            CommandUtils.sendError(source, "An unexpected error occurred during 'log stop' command!");
            CobbleAid.getLogger().error("Error executing 'log stop' command:", e);
            return 0;
        }

        return 1;
    }
}