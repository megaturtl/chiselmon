package cc.turtl.chiselmon;

import cc.turtl.chiselmon.command.*;
import cc.turtl.chiselmon.util.MessageUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import java.util.List;

public class ChiselmonCommands {
    private static final List<ChiselmonCommand> COMMANDS = List.of(
            new InfoCommand(),
            new DebugCommand(),
            new DatabaseCommand(),
            new AlertCommand(),
            new ConfigCommand(),
            new RecordCommand(),
            new DashCommand()
    );

    public static <S> void register(CommandDispatcher<S> dispatcher) {
        LiteralArgumentBuilder<S> root = LiteralArgumentBuilder.<S>literal(ChiselmonConstants.MOD_ID)
                .executes(ChiselmonCommands::showHelp);

        LiteralArgumentBuilder<S> rootAlias = LiteralArgumentBuilder.<S>literal("ch")
                .executes(ChiselmonCommands::showHelp);

        LiteralArgumentBuilder<S> legacyRootAlias = LiteralArgumentBuilder.<S>literal("ca")
                .executes(ChiselmonCommands::showHelp);

        COMMANDS.forEach(cmd -> root.then(cmd.build()));
        COMMANDS.forEach(cmd -> rootAlias.then(cmd.build()));
        COMMANDS.forEach(cmd -> legacyRootAlias.then(cmd.build()));

        dispatcher.register(root);
        dispatcher.register(rootAlias);
        dispatcher.register(legacyRootAlias);
    }

    public static <S> int showHelp(CommandContext<S> context) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return 0;

        // Gets the alias used
        String alias = context.getNodes().getFirst().getNode().getName();

        MessageUtils.sendEmptyLine(player);
        MessageUtils.sendSuccess(player, ChiselmonConstants.MOD_NAME + " Commands");
        COMMANDS.forEach(cmd ->
                MessageUtils.sendPrefixed(player, "  /" + alias + " " + cmd.getName() + " - " + cmd.getDescription())
        );
        return Command.SINGLE_SUCCESS;
    }
}