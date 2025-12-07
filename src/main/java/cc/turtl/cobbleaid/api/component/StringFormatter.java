package cc.turtl.cobbleaid.api.component;

import net.minecraft.network.chat.Component;

public final class StringFormatter {
    private StringFormatter() {
    }

    private static final Component UNKNOWN = Component.literal("???").withColor(ComponentColor.DARK_GRAY);

    public static Component format(String string) {
        if (string == null)
            return UNKNOWN;
        return Component.literal(string).withColor(ComponentColor.WHITE);
    }

    public static Component formatLabeled(String label, Component value) {
        if (label == null)
            return UNKNOWN;
        return Component.literal(label + ": ").withColor(ComponentColor.LIGHT_GRAY)
                .append(value);
    }

    public static Component formatLabeled(String label, String value) {
        if (label == null)
            return UNKNOWN;
        return Component.literal(label + ": ").withColor(ComponentColor.LIGHT_GRAY)
                .append(format(value));
    }
}