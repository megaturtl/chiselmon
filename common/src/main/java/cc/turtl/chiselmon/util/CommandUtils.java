package cc.turtl.chiselmon.util;

import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.util.format.ColorUtils;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import static cc.turtl.chiselmon.util.format.ComponentUtils.*;

public class CommandUtils {

    public static final Component PREFIX = Component.empty()
            .append(Component.literal("\uD83D\uDEE0 CH")
                    .withColor(ColorUtils.PINK)
                    .withStyle(ChatFormatting.BOLD))
            .append(literal(" » ", ColorUtils.DARK_GRAY));

    private CommandUtils() {
    }

    /**
     * Executes a command string as the client.
     */
    public static void execute(String command) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            player.connection.sendCommand(command.startsWith("/") ? command.substring(1) : command);
        }
    }

    /**
     * Sends a colored string to the command source (no prefix).
     */
    public static void send(CommandSourceStack source, String message, int color) {
        source.sendSuccess(() -> literal(message, color), false);
    }

    /**
     * Sends a component message to the command source (no prefix).
     */
    public static void send(CommandSourceStack source, Component message) {
        source.sendSuccess(() -> message, false);
    }

    public static void sendPrefixed(CommandSourceStack source, String message) {
        send(source, PREFIX.copy().append(literal(message, ColorUtils.WHITE)));
    }

    public static void sendPrefixed(CommandSourceStack source, Component message) {
        send(source, PREFIX.copy().append(message));
    }

    /**
     * Sends a success message (green) to the command source.
     */
    public static void sendSuccess(CommandSourceStack source, String message) {
        sendPrefixed(source, literal(message, ColorUtils.GREEN));
    }

    /**
     * Sends a warning message (yellow) to the command source.
     */

    public static void sendWarning(CommandSourceStack source, String message) {
        sendPrefixed(source, literal(message, ColorUtils.YELLOW));
    }

    /**
     * Sends an error message (red).
     */
    public static void sendError(CommandContext<CommandSourceStack> context, Exception e) {
        String input = context.getInput();
        context.getSource().sendFailure(PREFIX.copy().append(literal("Error executing: " + input, ColorUtils.RED)));
        ChiselmonConstants.LOGGER.error("Error executing command '{}': ", input, e);
    }

    /**
     * Sends a decorative header with signal bars and a rainbow gradient title.
     * <p>Format: ▂ ▃ ▅ ▆ ▇  <b>Title</b>  ▇ ▆ ▅ ▃ ▂</p>
     *
     */
    public static void sendHeader(CommandSourceStack source, Component title) {
        Component prefix = literal("▂ ▃ ▅ ▆ ▇", ColorUtils.DARK_GRAY);
        Component suffix = literal("▇ ▆ ▅ ▃ ▂", ColorUtils.DARK_GRAY);
        Component toolIcon = literal(" \uD83D\uDEE0 ", ColorUtils.PINK).withStyle(ChatFormatting.BOLD);

        Component header = Component.empty()
                .append(prefix)
                .append(toolIcon)
                .append(title)
                .append(toolIcon)
                .append(suffix);

        send(source, header);
    }

    public static void sendHeader(CommandSourceStack source, String title) {
        sendHeader(source, literal(title, ColorUtils.WHITE));
    }

    /**
     * Sends a labeled value (Label: Value).
     */
    public static void sendLabeled(CommandSourceStack source, String label, Object value) {
        sendPrefixed(source, labelled(label, value));
    }
}
