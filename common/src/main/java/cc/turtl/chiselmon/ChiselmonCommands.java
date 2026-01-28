package cc.turtl.chiselmon;

import cc.turtl.chiselmon.util.CommandUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Simple command registry for /chiselmon subcommands.
 */
public class ChiselmonCommands {

    private static final List<Consumer<LiteralArgumentBuilder<CommandSourceStack>>> SUBCOMMANDS = new ArrayList<>();

    /**
     * Register a subcommand under /chiselmon.
     * <p>
     * Example:
     * ChiselmonCommands.addSub(root -> root
     * .then(Commands.literal("hello")
     * .executes(ctx -> {
     * ctx.getSource().sendSuccess(() -> Component.literal("Hello!"), false);
     * return 1;
     * })
     * )
     * );
     */
    public static void addSub(Consumer<LiteralArgumentBuilder<CommandSourceStack>> subcommand) {
        SUBCOMMANDS.add(subcommand);
    }

    /**
     * Registers the root command and its sub commands with the dispatcher.
     * Call this usings the platform-specific command registration.
     */
    public static void registerRoot(CommandDispatcher<CommandSourceStack> dispatcher) {

        // Define root command (executes an info command)
        LiteralArgumentBuilder<CommandSourceStack> rootCommand = Commands.literal(ChiselmonConstants.MOD_ID)
                .executes(ChiselmonCommands::executeInfo);

        // Add subcommands to the root
        for (Consumer<LiteralArgumentBuilder<CommandSourceStack>> subcommand : SUBCOMMANDS) {
            subcommand.accept(rootCommand);
        }

        // Register root command
        dispatcher.register(rootCommand);

        // Register aliases
        dispatcher.register(Commands.literal("ca").redirect(rootCommand.build()));
        dispatcher.register(Commands.literal("ch").redirect(rootCommand.build()));
        dispatcher.register(Commands.literal("chisel").redirect(rootCommand.build()));
    }

    private static int executeInfo(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        CommandUtils.sendHeader(source, ChiselmonConstants.MOD_NAME + " Info");
        CommandUtils.sendLabeled(source, "Version", ChiselmonConstants.VERSION);
        CommandUtils.sendLabeled(source, "Author", ChiselmonConstants.AUTHOR);
        return 1;
    }
}