package cc.turtl.cobbleaid.command;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.config.ModConfig;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

public class ConfigCommand {
    
    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {
        return literal("config")
                .executes(ConfigCommand::executeHelp)
                .then(literal("enable").executes(ConfigCommand::executeEnable))
                .then(literal("disable").executes(ConfigCommand::executeDisable))
                .then(literal("status").executes(ConfigCommand::executeStatus));
    }

    private static int executeHelp(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Component.literal("§6§l=== Config Commands ==="));
        context.getSource().sendFeedback(Component.literal("§7/cobbleaid config enable §f- Enable the mod"));
        context.getSource().sendFeedback(Component.literal("§7/cobbleaid config disable §f- Disable the mod"));
        context.getSource().sendFeedback(Component.literal("§7/cobbleaid config status §f- Show current status"));
        return 1;
    }

    private static int executeEnable(CommandContext<FabricClientCommandSource> context) {
        ModConfig config = CobbleAid.getInstance().getConfig();
        config.modDisabled = false;
        CobbleAid.getInstance().saveConfig();

        context.getSource().sendFeedback(Component.literal("§aCobble Aid has been enabled!"));
        return 1;
    }

    private static int executeDisable(CommandContext<FabricClientCommandSource> context) {
        ModConfig config = CobbleAid.getInstance().getConfig();
        config.modDisabled = true;
        CobbleAid.getInstance().saveConfig();   

        context.getSource().sendFeedback(Component.literal("§cCobble Aid has been disabled!"));
        return 1;
    }

    private static int executeStatus(CommandContext<FabricClientCommandSource> context) {
        ModConfig config = CobbleAid.getInstance().getConfig();
        String status = config.modDisabled ? "§cDisabled" : "§aEnabled";
        context.getSource().sendFeedback(Component.literal("§7Cobble Aid Status: " + status));
        return 1;
    }
}