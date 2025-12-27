package cc.turtl.cobbleaid.feature.spawnlogger;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;
import cc.turtl.cobbleaid.util.CommandUtils;
import cc.turtl.cobbleaid.CobbleAid;

public class SpawnLoggerCommand {
    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {

        
        var startBranch = literal("start")
                .then(argument("minutes", IntegerArgumentType.integer(1, 60))
                        .executes(SpawnLoggerCommand::executeStart));

        var pauseBranch = literal("pause")
                .executes(SpawnLoggerCommand::executePause);
        
        var cancelBranch = literal("cancel")
                .executes(SpawnLoggerCommand::executeCancel);

        return literal("log")
                .executes(SpawnLoggerCommand::executeHelp)
                .then(startBranch)
                .then(pauseBranch)
                .then(cancelBranch);
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
        int mins = IntegerArgumentType.getInteger(context, "minutes");
        SpawnLoggerFeature spawnLogger = SpawnLoggerFeature.getInstance();

        if (spawnLogger.getSession() != null) {
            CommandUtils.sendWarning(source, "Spawn Logger already running!");
            return 1;
        }

        try {
            spawnLogger.startSession(mins);
            CommandUtils.sendSuccess(source, "Spawn Logger session started, running for " + mins + " mins");
        } catch (Exception e) {
            CommandUtils.sendError(source, "An unexpected error occurred during 'log start' command!");
            CobbleAid.getLogger().error("Error executing 'log start' command:", e);
            return 0;
        }

        return 1;
    }

    private static int executePause(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        SpawnLoggerFeature spawnLogger = SpawnLoggerFeature.getInstance();

        if (spawnLogger.getSession() == null) {
            CommandUtils.sendWarning(source, "No Spawn Logger currently running!");
            return 1;
        }

        try {
            boolean paused = spawnLogger.toggleSessionPause();
            String status = paused ? "Paused" : "Resumed";

            CommandUtils.sendToggle(source,
                    "Spawn Logger " + status + ". (" + spawnLogger.getSession().getRemainingSeconds() + "s left)",
                    !paused);
        } catch (Exception e) {
            CommandUtils.sendError(source, "An unexpected error occurred during 'log pause' command!");
            CobbleAid.getLogger().error("Error executing 'log pause' command:", e);
            return 0;
        }

        return 1;
    }

    private static int executeCancel(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        SpawnLoggerFeature spawnLogger = SpawnLoggerFeature.getInstance();

        if (spawnLogger.getSession() == null) {
            CommandUtils.sendWarning(source, "No Spawn Logger currently running!");
            return 1;
        }

        try {
            spawnLogger.finishSession();
        } catch (Exception e) {
            CommandUtils.sendError(source, "An unexpected error occurred during 'log cancel' command!");
            CobbleAid.getLogger().error("Error executing 'log cancel' command:", e);
            return 0;
        }

        return 1;
    }
}