package cc.turtl.cobbleaid.command;

import cc.turtl.cobbleaid.util.ComponentFormatUtil;
import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.util.ColorUtil;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

/**
 * Helper utility for consistent command feedback formatting.
 *
 * Centralizes formatting so commands are smaller and consistent.
 *
 * Note: sendInfo was removed in favor of sendLabeled which uses ComponentFormatUtil.labelledValue.
 */
public class CommandFeedbackHelper {

    private static final String PREFIX = "[" + CobbleAid.MODID + "] ";

    private CommandFeedbackHelper() {
    }

    /**
     * Sends a header message with purple color
     */
    public static void sendHeader(FabricClientCommandSource source, String title) {
        source.sendFeedback(ComponentFormatUtil.colored("=== " + title + " ===", ColorUtil.PURPLE));
    }

    /**
     * Sends a command usage message with gray color
     */
    public static void sendUsage(FabricClientCommandSource source, String command) {
        source.sendFeedback(ComponentFormatUtil.colored(command, ColorUtil.LIGHT_GRAY));
    }

    /**
     * Sends a command usage message with gray command and white description
     */
    public static void sendUsageWithDescription(FabricClientCommandSource source, String command, String description) {
        source.sendFeedback(
                ComponentFormatUtil.colored(command + " ", ColorUtil.LIGHT_GRAY)
                        .append(ComponentFormatUtil.colored("- " + description, ColorUtil.WHITE)));
    }

    /**
     * Sends a success message with green color
     */
    public static void sendSuccess(FabricClientCommandSource source, String message) {
        source.sendFeedback(ComponentFormatUtil.colored(message, ColorUtil.GREEN));
    }

    /**
     * Sends an error message with red color and [Cobble Aid] prefix
     */
    public static void sendError(FabricClientCommandSource source, String message) {
        source.sendFeedback(
                ComponentFormatUtil.colored(PREFIX, ColorUtil.RED).append(ComponentFormatUtil.colored(message, ColorUtil.WHITE)));
    }

    /**
     * Sends a warning/info message with yellow color
     */
    public static void sendWarning(FabricClientCommandSource source, String message) {
        source.sendFeedback(ComponentFormatUtil.colored(message, ColorUtil.YELLOW));
    }

    /**
     * Sends a labeled Component (label colored light gray, value white).
     */
    public static void sendLabeled(FabricClientCommandSource source, String label, Component value) {
        source.sendFeedback(ComponentFormatUtil.labelledValue(label, value));
    }

    /**
     * Sends a labeled string value.
     */
    public static void sendLabeled(FabricClientCommandSource source, String label, String value) {
        source.sendFeedback(ComponentFormatUtil.labelledValue(label, value));
    }
}