package cc.turtl.chiselmon.util;

import org.jetbrains.annotations.Nullable;

import com.mojang.brigadier.context.CommandContext;

import cc.turtl.chiselmon.Chiselmon;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public class CommandUtils {

    private static final String PREFIX = "[" + Chiselmon.MODID + "] ";

    private CommandUtils() {
    }

    public static void executeClientCommand(String command) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            player.connection.sendCommand(command.startsWith("/") ? command.substring(1) : command);
        }
    }

    public static int executeWithErrorHandling(
            CommandContext<FabricClientCommandSource> context,
            CommandExecutor executor) {
        FabricClientCommandSource source = context.getSource();
        try {
            executor.execute(source);
            return 1;
        } catch (Exception e) {
            CommandUtils.sendError(source, "An unexpected error occurred.");
            Chiselmon.getLogger().error("Error executing '" + context.getInput() + "' :", e);
            return 0;
        }
    }
    @FunctionalInterface
    public interface CommandExecutor {
        void execute(FabricClientCommandSource source) throws Exception;
    }

    /**
     * Sends a generic colored message
     */
    public static void sendColored(FabricClientCommandSource source, String message, int color) {
        source.sendFeedback(ComponentUtil.colored(message, color));
    }

    /**
     * Sends a header message with purple color
     */
    public static void sendHeader(FabricClientCommandSource source, String title) {
        source.sendFeedback(ComponentUtil.colored("=== " + title + " ===", ColorUtil.PURPLE));
    }

    /**
     * Sends a command usage message with gray color
     */
    public static void sendUsage(FabricClientCommandSource source, String command) {
        source.sendFeedback(ComponentUtil.colored(command, ColorUtil.LIGHT_GRAY));
    }

    /**
     * Sends a command usage message with gray command and white description
     */
    public static void sendUsageWithDescription(FabricClientCommandSource source, String command, String description) {
        source.sendFeedback(
                ComponentUtil.colored(command + " ", ColorUtil.LIGHT_GRAY)
                        .append(ComponentUtil.colored("- " + description, ColorUtil.WHITE)));
    }

    /**
     * Sends a success message with green color
     */
    public static void sendSuccess(FabricClientCommandSource source, String message) {
        source.sendFeedback(ComponentUtil.colored(message, ColorUtil.GREEN));
    }

    public static void sendToggle(FabricClientCommandSource source, String message, boolean toggleOn) {
        int color = toggleOn ? ColorUtil.GREEN : ColorUtil.ORANGE;
        source.sendFeedback(ComponentUtil.colored(message, color));
    }

    /**
     * Sends an error message with red color and prefix
     */
    public static void sendError(FabricClientCommandSource source, String message) {
        source.sendFeedback(
                ComponentUtil.colored(PREFIX, ColorUtil.RED)
                        .append(ComponentUtil.colored(message, ColorUtil.WHITE)));
    }

    /**
     * Sends a warning/info message with yellow color
     */
    public static void sendWarning(FabricClientCommandSource source, String message) {
        source.sendFeedback(ComponentUtil.colored(message, ColorUtil.YELLOW));
    }

    /**
     * Sends a labeled string value (label colored light gray, value white).
     */
    public static void sendLabeled(FabricClientCommandSource source, String label, @Nullable Object value) {
        source.sendFeedback(ComponentUtil.labelledValue(label + ": ", value));
    }

}