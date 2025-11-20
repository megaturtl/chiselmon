package cc.turtl.cobbleaid.command;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

import cc.turtl.cobbleaid.config.ConfigManager;
import cc.turtl.cobbleaid.config.ModConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;

public class CobbleAidCommand {
    private final ConfigManager configManager;
    private final ModConfig config;

    public CobbleAidCommand(ConfigManager configManager, ModConfig config) {
        this.configManager = configManager;
        this.config = config;
    }

    public void register() {
        ClientCommandRegistrationCallback.EVENT.register(this::registerCommand);
    }

    private void registerCommand(
            CommandDispatcher<FabricClientCommandSource> dispatcher,
            CommandBuildContext registryAccess) {
        dispatcher.register(
                literal("cobbleaid")
                        .executes(this::executeHelp)
                        .then(literal("enable").executes(this::executeEnable))
                        .then(literal("disable").executes(this::executeDisable)));
    }

    private int executeHelp(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Component.literal("§6Cobble Aid Commands:"));
        context.getSource().sendFeedback(Component.literal("§7/cobbleaid enable §f- Enable the mod"));
        context.getSource().sendFeedback(Component.literal("§7/cobbleaid disable §f- Disable the mod"));
        return 1;
    }

    private int executeEnable(CommandContext<FabricClientCommandSource> context) {
        config.modDisabled = false;
        configManager.save();

        context.getSource().sendFeedback(Component.literal("§aCobble Aid has been enabled!"));
        return 1;
    }

    private int executeDisable(CommandContext<FabricClientCommandSource> context) {
        config.modDisabled = true;
        configManager.save();

        context.getSource().sendFeedback(Component.literal("§cCobble Aid has been disabled!"));
        return 1;
    }
}
