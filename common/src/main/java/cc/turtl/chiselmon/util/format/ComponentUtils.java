package cc.turtl.chiselmon.util.format;

import cc.turtl.chiselmon.ChiselmonConstants;
import net.minecraft.ChatFormatting;
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
    /**
     * Placeholder component for missing or null data.
     */
    public static final Component UNKNOWN = createComponent("???", ColorUtils.DARK_GRAY.getRGB());
    public static final Component NONE = createComponent("None", ColorUtils.DARK_GRAY.getRGB());
    public static final Component SPACE = Component.literal(" ");
    public static final Component RESET = Component.literal("").withStyle(ChatFormatting.RESET);

    private ComponentUtils() {
    }

    /**
     * Creates a translatable component prefixed with the mod ID.
     * <p>Example: {@code translatable("msg.caught", "Pikachu")} maps to
     * {@code "chiselmon.msg.caught": "Caught %s!"} in lang files.</p>
     */
    public static MutableComponent modTranslatable(String key, Object... args) {
        return Component.translatable(ChiselmonConstants.MOD_ID + "." + key, args);
    }

    public static MutableComponent createComponent(Object text) {
        return createComponent(text, ColorUtils.WHITE.getRGB());
    }

    /**
     * Creates a component with a specific color.
     * <p>Example: {@code literal("Lvl 50", ColorUtil.GOLD)}</p>
     */
    public static MutableComponent createComponent(Object text, int color) {
        return createComponent(text, color, false);
    }

    public static MutableComponent createComponent(Object text, int color, boolean bold) {
        String content = text == null ? "" : text.toString();

        MutableComponent component = Component.literal(content);

        return component.withStyle(style -> {
            style = style.withColor(color).withBold(bold);
            return style;
        });
    }

    /**
     * Creates a component from a label and value pair.
     * <p>Example: {@code label("Ability", "Intimidate")} -> "Ability: Intimidate"</p>
     */
    public static Component labelled(@NotNull Object label, @Nullable Object value) {
        MutableComponent labelComp = (label instanceof Component c ? c.copy() : Component.literal(label.toString()));
        // If we pass a component label, it gets overridden with gray for label consistency
        labelComp.withColor(ColorUtils.LIGHT_GRAY.getRGB());

        Component valueComp = (value == null) ? UNKNOWN :
                (value instanceof Component c ? c : createComponent(value.toString(), ColorUtils.WHITE.getRGB()));

        return labelComp.append(createComponent(": ", ColorUtils.LIGHT_GRAY.getRGB())).append(valueComp);
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
                if (it.hasNext()) result.append(createComponent(separator, ColorUtils.DARK_GRAY.getRGB()));
            }
        }
        return result;
    }

    /**
     * Creates a component where the text is colored with a multi-point gradient.
     * * @param text   The string to color.
     *
     * @param colors The RGB color stops (0xRRGGBB).
     * @return A MutableComponent containing the gradient text.
     */
    public static MutableComponent gradient(String text, int... colors) {
        if (text == null || text.isEmpty()) return Component.empty();
        if (colors.length == 0) return Component.literal(text);
        if (colors.length == 1) return createComponent(text, colors[0]);

        MutableComponent result = Component.empty();
        int length = text.length();

        for (int i = 0; i < length; i++) {
            // Calculate ratio (0.0 to 1.0) based on character index
            // If length is 1, ratio is 0.
            float ratio = length > 1 ? (float) i / (length - 1) : 0.0f;

            int color = ColorUtils.getGradient(ratio, colors);
            result.append(Component.literal(String.valueOf(text.charAt(i))).withColor(color));
        }

        return result;
    }
}