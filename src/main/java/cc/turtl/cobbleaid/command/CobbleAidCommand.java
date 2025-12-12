package cc.turtl.cobbleaid.command;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import cc.turtl.cobbleaid.CobbleAid;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;

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
                .then(EggCommand.register());

        dispatcher.register(baseCommand);
        dispatcher.register(literal("ca").redirect(baseCommand.build()));
    }

    private static int executeHelp(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        CommandFeedbackHelper.sendHeader(source, "Cobble Aid Commands");
        CommandFeedbackHelper.sendUsage(source, "/" + CobbleAid.MODID + " info");
        CommandFeedbackHelper.sendUsage(source, "/" + CobbleAid.MODID + " config");
        CommandFeedbackHelper.sendUsage(source, "/" + CobbleAid.MODID + " debug");
        CommandFeedbackHelper.sendUsage(source, "/" + CobbleAid.MODID + " egg");

        return 1;
    }
}