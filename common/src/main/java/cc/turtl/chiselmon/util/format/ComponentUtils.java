package cc.turtl.chiselmon.util.format;

import cc.turtl.chiselmon.ChiselmonConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.function.Function;

/**
 * Provides basic chat component formatting utils.
 */
public final class ComponentUtils {
    private ComponentUtils() {}

    /** Placeholder component for missing or null data. */
    public static final Component UNKNOWN = literal("???", ColorUtils.DARK_GRAY);

    public static final Component SPACE = Component.literal(" ");

    /**
     * Creates a translatable component prefixed with the mod ID.
     * <p>Example: {@code translatable("msg.caught", "Pikachu")} maps to
     * {@code "chiselmon.msg.caught": "Caught %s!"} in lang files.</p>
     */
    public static MutableComponent modTranslatable(String key, Object... args) {
        return Component.translatable(ChiselmonConstants.MOD_ID + "." + key, args);
    }

    /**
     * Creates a literal component with a specific color.
     * <p>Example: {@code literal("Lvl 50", ColorUtil.GOLD)}</p>
     */
    public static MutableComponent literal(Object text, int color) {
        return Component.literal(text.toString()).withColor(color);
    }

    /**
     * Creates a component from a label and value pair.
     * <p>Example: {@code label("Ability", "Intimidate")} -> "Ability: Intimidate"</p>
     */
    public static Component labelled(@NotNull Object label, @Nullable Object value) {
        MutableComponent labelComp = (label instanceof Component c ? c.copy() : Component.literal(label.toString()));
        // If we pass a component label, it gets overridden with gray for label consistency
        labelComp.withColor(ColorUtils.LIGHT_GRAY);

        Component valueComp = (value == null) ? UNKNOWN :
                (value instanceof Component c ? c : literal(value.toString(), ColorUtils.WHITE));

        return labelComp.append(literal(": ", ColorUtils.LIGHT_GRAY)).append(valueComp);
    }

    /**
     * Joins items into a single component with separators.
     * <p>Example: {@code join(list, ", ", item -> literal(item, ColorUtil.RED))}</p>
     */
    public static <E> Component join(@Nullable Iterable<E> items, String separator, Function<E, Component> mapper) {
        if (items == null || !items.iterator().hasNext()) return UNKNOWN;

        MutableComponent result = Component.empty();
        Iterator<E> it = items.iterator();

        while (it.hasNext()) {
            Component mapped = mapper.apply(it.next());
            if (mapped != null) {
                result.append(mapped);
                if (it.hasNext()) result.append(literal(separator, ColorUtils.LIGHT_GRAY));
            }
        }
        return result;
    }
}