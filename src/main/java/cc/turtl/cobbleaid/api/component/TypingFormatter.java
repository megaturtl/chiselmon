package cc.turtl.cobbleaid.api.component;

import com.cobblemon.mod.common.api.types.ElementalType;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class TypingFormatter {
    private TypingFormatter() {
    }

    private static final Component UNKNOWN = Component.literal("???").withColor(ComponentColor.DARK_GRAY);
    private static final Component SEPARATOR = Component.literal(" / ").withColor(ComponentColor.LIGHT_GRAY);

    public static Component format(ElementalType type) {
        if (type == null)
            return UNKNOWN;
        return Component.literal(type.getName()).withColor(type.getHue());
    }

    public static Component format(ElementalType type1, ElementalType type2) {
        if (type1 == null)
            return UNKNOWN;

        MutableComponent result = Component.literal(type1.getName()).withColor(type1.getHue());

        if (type2 != null) {
            result.append(SEPARATOR).append(Component.literal(type2.getName()).withColor(type2.getHue()));
        }

        return result;
    }

    public static Component formatLabeled(ElementalType type) {
        return StringFormatter.formatLabeled("Type", format(type));
    }

    public static Component formatLabeled(ElementalType type1, ElementalType type2) {
        return StringFormatter.formatLabeled("Type", format(type1, type2));
    }
}