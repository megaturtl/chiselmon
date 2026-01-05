package cc.turtl.chiselmon.command;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import cc.turtl.chiselmon.Chiselmon;
import cc.turtl.chiselmon.feature.spawnalert.SpawnAlertCommand;
import cc.turtl.chiselmon.feature.spawnlogger.SpawnLoggerCommand;
import cc.turtl.chiselmon.util.CommandUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;

public class ChiselmonCommand {
    
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register(ChiselmonCommand::registerCommand);
    }

    private static void registerCommand(
            CommandDispatcher<FabricClientCommandSource> dispatcher,
            CommandBuildContext registryAccess) {

        var baseCommand = literal(Chiselmon.MODID)
                .executes(ChiselmonCommand::executeHelp)
                .then(InfoCommand.register())
                .then(DebugCommand.register())
                .then(EggCommand.register())
                .then(SpawnAlertCommand.register())
                .then(SpawnLoggerCommand.register());

        dispatcher.register(baseCommand);
        dispatcher.register(literal("chisel").redirect(baseCommand.build()));
    }

    private static int executeHelp(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        CommandUtils.sendHeader(source, "Commands");
        CommandUtils.sendUsage(source, "/" + Chiselmon.MODID + " info");
        CommandUtils.sendUsage(source, "/" + Chiselmon.MODID + " config");
        CommandUtils.sendUsage(source, "/" + Chiselmon.MODID + " debug");
        CommandUtils.sendUsage(source, "/" + Chiselmon.MODID + " egg");
        CommandUtils.sendUsage(source, "/" + Chiselmon.MODID + " alert");
        CommandUtils.sendUsage(source, "/" + Chiselmon.MODID + " log");

        return 1;
    }
}