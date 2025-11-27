package cc.turtl.cobbleaid.command;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;

public class CobbleAidCommand {
    
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register(CobbleAidCommand::registerCommand);
    }

    private static void registerCommand(
            CommandDispatcher<FabricClientCommandSource> dispatcher,
            CommandBuildContext registryAccess) {
        
        dispatcher.register(
                literal("cobbleaid")
                        .executes(CobbleAidCommand::executeHelp)
                        .then(InfoCommand.register())
                        .then(ConfigCommand.register())
                        .then(DebugCommand.register())
                        .then(EggCommand.register())
        );

        dispatcher.register(
                literal("ca")
                        .executes(CobbleAidCommand::executeHelp)
                        .then(InfoCommand.register())
                        .then(ConfigCommand.register())
                        .then(DebugCommand.register())
                        .then(EggCommand.register())
        );
    }

    private static int executeHelp(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Component.literal("§d=== Cobble Aid Commands ==="));
        context.getSource().sendFeedback(Component.literal("§7/cobbleaid info"));
        context.getSource().sendFeedback(Component.literal("§7/cobbleaid config"));
        context.getSource().sendFeedback(Component.literal("§7/cobbleaid debug"));
        context.getSource().sendFeedback(Component.literal("§7/cobbleaid egg"));

        return 1;
    }
}