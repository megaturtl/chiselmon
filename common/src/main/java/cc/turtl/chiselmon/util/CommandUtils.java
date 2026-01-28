package cc.turtl.chiselmon.util;

import cc.turtl.chiselmon.util.format.ColorUtils;
import cc.turtl.chiselmon.util.format.ComponentUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.awt.*;

public class CommandUtils {

    private static final MutableComponent PREFIX = ComponentUtils.literal("[\uD83D\uDEE0]", ColorUtils.LAVENDER);

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
        source.sendSuccess(() -> ComponentUtils.literal(message, color), false);
    }

    /**
     * Sends a component message to the command source (no prefix).
     */
    public static void send(CommandSourceStack source, Component message) {
        source.sendSuccess(() -> message, false);
    }

    /**
     * Sends a success message (green) to the command source.
     */
    public static void sendSuccess(CommandSourceStack source, String message) {
        send(source, PREFIX.append(ComponentUtils.literal(message, ColorUtils.GREEN)));
    }

    /**
     * Sends a warning message (yellow) to the command source.
     */

    public static void sendWarning(CommandSourceStack source, String message) {
        send(source, PREFIX.append(ComponentUtils.literal(message, ColorUtils.YELLOW)));
    }

    /**
     * Sends an error message (red).
     */
    public static void sendError(CommandSourceStack source, String message) {
        source.sendFailure(PREFIX.append(ComponentUtils.literal(message, ColorUtils.RED)));
    }

    /**
     * Sends a header (=== Title ===).
     */
    public static void sendHeader(CommandSourceStack source, String title) {
        send(source, "=== " + title + " ===", ColorUtils.LAVENDER);
    }

    /**
     * Sends a labeled value (Label: Value).
     */
    public static void sendLabeled(CommandSourceStack source, String label, Object value) {
        send(source, ComponentUtils.labelled(label, value));
    }
}
