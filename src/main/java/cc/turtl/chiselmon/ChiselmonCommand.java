package cc.turtl.chiselmon;

import cc.turtl.chiselmon.command.DebugCommand;
import cc.turtl.chiselmon.feature.spawnalert.SpawnAlertCommand;
import cc.turtl.chiselmon.feature.spawnlogger.SpawnLoggerCommand;
import cc.turtl.chiselmon.util.CommandUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ChiselmonCommand {

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register(ChiselmonCommand::registerCommand);
    }

    private static void registerCommand(
            CommandDispatcher<FabricClientCommandSource> dispatcher,
            CommandBuildContext registryAccess) {

        var baseCommand = literal(ChiselmonConstants.MODID).executes(ChiselmonCommand::executeHelp)
                .then(literal("info")).executes(ChiselmonCommand::executeInfo)
                .then(DebugCommand.register())
                .then(SpawnAlertCommand.register())
                .then(SpawnLoggerCommand.register());

        dispatcher.register(baseCommand);
        dispatcher.register(literal("chisel").redirect(baseCommand.build()));
        dispatcher.register(literal("ca").redirect(baseCommand.build()));
    }

    private static int executeHelp(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        CommandUtils.sendHeader(source, "Commands");
        CommandUtils.sendUsage(source, "/" + ChiselmonConstants.MODID + " info");
        CommandUtils.sendUsage(source, "/" + ChiselmonConstants.MODID + " debug");
        CommandUtils.sendUsage(source, "/" + ChiselmonConstants.MODID + " alert");
        CommandUtils.sendUsage(source, "/" + ChiselmonConstants.MODID + " log");

        return 1;
    }

    private static int executeInfo(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        CommandUtils.sendHeader(source, ChiselmonConstants.MODNAME + " Info");
        CommandUtils.sendLabeled(source, "Version", ChiselmonConstants.VERSION);
        CommandUtils.sendLabeled(source, "Author", "megaturtl");
        return 1;
    }
}