package cc.turtl.cobbleaid.command;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

import cc.turtl.cobbleaid.CobbleAid;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

public class InfoCommand {
    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {
        return literal("info")
                .executes(InfoCommand::execute);
    }
    private static int execute(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Component.literal("§6=== Cobble Aid Info ==="));
        context.getSource().sendFeedback(Component.literal("§7Version: §f" + CobbleAid.VERSION));
        context.getSource().sendFeedback(Component.literal("§7Author: §fmegaturtl"));
        return 1;
    }
    
}
