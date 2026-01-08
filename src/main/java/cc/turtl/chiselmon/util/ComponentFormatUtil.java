package cc.turtl.chiselmon.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import cc.turtl.chiselmon.ChiselmonConstants;

import java.util.Collection;
import java.util.function.Function;

/**
 * Base utility class for formatters providing common formatting utilities.
 */
public class ComponentFormatUtil {

    public static final Component UNKNOWN = colored("???", ColorUtil.DARK_GRAY);

    public static MutableComponent translatable(String key) {
        return Component.translatable(ChiselmonConstants.MODID + "." + key);
    }

    public static MutableComponent colored(String text, int color) {
        return Component.literal(text).withColor(color);
    }

    public static MutableComponent colored(MutableComponent component, int color) {
        return component.withColor(color);
    }

    public static Component separator(String text) {
        return colored(text, ColorUtil.LIGHT_GRAY);
    }

    public static <E> Component buildComponentWithSeparator(
            @Nullable Iterable<E> items,
            @NotNull Component separator,
            @NotNull Function<E, Component> mapper) {

        if (items == null) {
            return UNKNOWN;
        }

        MutableComponent result = Component.empty();
        boolean first = true;
        boolean hasContent = false;

        for (E item : items) {
            Component mappedComponent = mapper.apply(item);

            // Only append separator if it's not the first item and the mapped component
            // isn't empty/null
            if (mappedComponent != null && !mappedComponent.getString().isEmpty()) {
                if (!first) {
                    result.append(separator);
                }
                result.append(mappedComponent);
                first = false;
                hasContent = true;
            }
        }

        return hasContent ? result : UNKNOWN;
    }

    public static Component buildComponentFromStrings(Collection<String> strings, int color) {
        if (strings == null) {
            return UNKNOWN;
        }

        MutableComponent result = Component.empty();
        for (String string : strings) {
            result.append(colored(string, color));
        }

        return result.getString().isEmpty() ? UNKNOWN : result;
    }

    public static Component labelledValue(@NotNull String label, @Nullable Object value) {
        MutableComponent labelComponent = colored(label, ColorUtil.LIGHT_GRAY);

        Component valueComponent = (value == null)
                ? UNKNOWN
                : (value instanceof Component c)
                        ? c
                        : colored(value.toString(), ColorUtil.WHITE);

        return labelComponent.append(valueComponent);
    }

    private ComponentFormatUtil() {
    }
}