package cc.turtl.cobbleaid.command;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

/**
 * Helper utility for consistent command feedback formatting
 */
public class CommandFeedbackHelper {

    private CommandFeedbackHelper() {
    }

    /**
     * Sends a header message with purple color
     */
    public static void sendHeader(FabricClientCommandSource source, String title) {
        source.sendFeedback(Component.literal("§d=== " + title + " ==="));
    }

    /**
     * Sends a command usage message with gray color
     */
    public static void sendUsage(FabricClientCommandSource source, String command) {
        source.sendFeedback(Component.literal("§7" + command));
    }

    /**
     * Sends a command usage message with gray command and white description
     */
    public static void sendUsageWithDescription(FabricClientCommandSource source, String command, String description) {
        source.sendFeedback(Component.literal("§7" + command + " §f- " + description));
    }

    /**
     * Sends a success message with green color
     */
    public static void sendSuccess(FabricClientCommandSource source, String message) {
        source.sendFeedback(Component.literal("§a" + message));
    }

    /**
     * Sends an error message with red color and [Cobble Aid] prefix
     */
    public static void sendError(FabricClientCommandSource source, String message) {
        source.sendFeedback(Component.literal("§c[Cobble Aid] " + message));
    }

    /**
     * Sends a warning/info message with yellow color
     */
    public static void sendWarning(FabricClientCommandSource source, String message) {
        source.sendFeedback(Component.literal("§e" + message));
    }

    /**
     * Sends an info message with gray label and custom value
     */
    public static void sendInfo(FabricClientCommandSource source, String label, String value) {
        source.sendFeedback(Component.literal("§7" + label + ": §f" + value));
    }
}
