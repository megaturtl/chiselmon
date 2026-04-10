package cc.turtl.chiselmon.util;

import cc.turtl.chiselmon.core.ChiselmonConstants;
import cc.turtl.chiselmon.util.format.ColorUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import static cc.turtl.chiselmon.util.format.ComponentUtils.createComponent;
import static cc.turtl.chiselmon.util.format.ComponentUtils.labelled;

public class MessageUtils {
    private MessageUtils() {
    }

    public static void executeCommand(@NotNull LocalPlayer player, String command) {
        player.connection.sendCommand(command.startsWith("/") ? command.substring(1) : command);
    }

    public static void sendEmptyLine(@NotNull LocalPlayer player) {
        send(player, Component.empty());
    }

    public static void send(@NotNull LocalPlayer player, Component message) {
        player.sendSystemMessage(message);
    }

    public static void send(@NotNull LocalPlayer player, String message) {
        send(player, Component.literal(message));
    }

    public static void sendPrefixed(@NotNull LocalPlayer player, Component message) {
        send(player, Component.empty().append(ChiselmonConstants.MESSAGE_PREFIX).append(message));
    }

    public static void sendPrefixed(@NotNull LocalPlayer player, String message) {
        sendPrefixed(player, createComponent(message, ColorUtils.WHITE.getRGB()));
    }

    public static void sendSuccess(@NotNull LocalPlayer player, String message) {
        sendPrefixed(player, createComponent(message, ColorUtils.GREEN.getRGB()));
    }

    public static void sendWarning(@NotNull LocalPlayer player, String message) {
        sendPrefixed(player, createComponent(message, ColorUtils.YELLOW.getRGB()));
    }

    public static void sendError(@NotNull LocalPlayer player, Exception e) {
        sendPrefixed(player, createComponent("An error occurred with that command.", ColorUtils.RED.getRGB()));
        ChiselmonConstants.LOGGER.error("Error occured while executing command: ", e);
    }

    public static void sendLabeled(@NotNull LocalPlayer player, String label, Object value) {
        sendPrefixed(player, labelled(label, value));
    }
}