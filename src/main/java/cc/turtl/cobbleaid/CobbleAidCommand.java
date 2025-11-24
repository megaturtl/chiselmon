package cc.turtl.cobbleaid;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

import cc.turtl.cobbleaid.command.*;
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
                        .then(literal("help").executes(CobbleAidCommand::executeHelp))
                        .then(InfoCommand.register())
                        .then(ConfigCommand.register())
                        .then(DebugCommand.register())
        );

        dispatcher.register(
                literal("ca")
                        .executes(CobbleAidCommand::executeHelp)
                        .then(literal("help").executes(CobbleAidCommand::executeHelp))
                        .then(InfoCommand.register())
                        .then(ConfigCommand.register())
                        .then(DebugCommand.register())
        );
    }

    private static int executeHelp(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Component.literal("§6§l=== Cobble Aid Help ==="));
        context.getSource().sendFeedback(Component.literal("§7/cobbleaid help §f- Show this help message"));
        context.getSource().sendFeedback(Component.literal("§7/cobbleaid reload §f- Reload configuration"));
        context.getSource().sendFeedback(Component.literal("§7/cobbleaid config §f- Configuration commands"));
        context.getSource().sendFeedback(Component.literal("§7/cobbleaid debug §f- Debug commands"));
        context.getSource().sendFeedback(Component.literal("§8Alias: §7/ca"));
        return 1;
    }
}