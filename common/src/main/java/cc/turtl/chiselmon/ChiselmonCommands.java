package cc.turtl.chiselmon;

import cc.turtl.chiselmon.command.*;
import cc.turtl.chiselmon.util.MessageUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.List;

public class ChiselmonCommands {
    private static final List<ChiselmonCommand> COMMANDS = List.of(
            new InfoCommand(),
            new DebugCommand(),
            new TrackerCommand(),
            new AlertCommand(),
            new ConfigCommand()
    );

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                                CommandBuildContext registry,
                                Commands.CommandSelection selection) {

        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(ChiselmonConstants.MOD_ID)
                .executes(ChiselmonCommands::showHelp);

        LiteralArgumentBuilder<CommandSourceStack> rootAlias = Commands.literal("ch")
                .executes(ChiselmonCommands::showHelp);

        // Attach all subcommands
        COMMANDS.forEach(cmd -> root.then(cmd.build()));
        COMMANDS.forEach(cmd -> rootAlias.then(cmd.build()));

        // Register the main command and alias
        dispatcher.register(root);
        dispatcher.register(rootAlias);
    }

    private static int showHelp(CommandContext<CommandSourceStack> context) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return 0;

        // Gets the alias used
        String alias = context.getNodes().getFirst().getNode().getName();

        MessageUtils.sendHeader(player, ChiselmonConstants.MOD_NAME + " Commands");
        COMMANDS.forEach(cmd ->
                MessageUtils.sendPrefixed(player, "/" + alias + " " + cmd.getName() + " - " + cmd.getDescription())
        );
        return Command.SINGLE_SUCCESS;
    }
}