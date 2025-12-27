package cc.turtl.cobbleaid.command;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.feature.spawnalert.SpawnAlertCommand;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import cc.turtl.cobbleaid.util.CommandUtils;

public class CobbleAidCommand {
    
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register(CobbleAidCommand::registerCommand);
    }

    private static void registerCommand(
            CommandDispatcher<FabricClientCommandSource> dispatcher,
            CommandBuildContext registryAccess) {

        var baseCommand = literal(CobbleAid.MODID)
                .executes(CobbleAidCommand::executeHelp)
                .then(InfoCommand.register())
                .then(ConfigCommand.register())
                .then(DebugCommand.register())
                .then(EggCommand.register())
                .then(SpawnAlertCommand.register());

        dispatcher.register(baseCommand);
        dispatcher.register(literal("ca").redirect(baseCommand.build()));
    }

    private static int executeHelp(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        CommandUtils.sendHeader(source, "Cobble Aid Commands");
        CommandUtils.sendUsage(source, "/" + CobbleAid.MODID + " info");
        CommandUtils.sendUsage(source, "/" + CobbleAid.MODID + " config");
        CommandUtils.sendUsage(source, "/" + CobbleAid.MODID + " debug");
        CommandUtils.sendUsage(source, "/" + CobbleAid.MODID + " egg");

        return 1;
    }
}