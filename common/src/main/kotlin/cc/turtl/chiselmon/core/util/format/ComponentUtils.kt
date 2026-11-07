package cc.turtl.chiselmon.core.util.format

import cc.turtl.turtlshell.api.core.format.ColorLib
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

/** Placeholder component for missing or null data. */
val UNKNOWN: Component = createComponent("???", ColorLib.DARK_GRAY.rgb)
val NONE: Component = createComponent("None", ColorLib.DARK_GRAY.rgb)
val SPACE: Component = Component.literal(" ")

fun createComponent(text: Any?): MutableComponent = createComponent(text, ColorLib.WHITE.rgb)

/**
 * Creates a component with a specific color.
 *
 * Example: `literal("Lvl 50", ColorLib.GOLD)`
 */
fun createComponent(
    text: Any?,
    color: Int,
    bold: Boolean = false,
): MutableComponent {
    val content = text?.toString() ?: ""
    val component = Component.literal(content)

    return component.withStyle { style ->
        style.withColor(color).withBold(bold)
    }
}

/** Creates a clickable URL component that opens the link in the browser when clicked. */
fun clickableUrl(url: String): Component =
    Component.literal(url).withStyle { style ->
        style
            .withClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, url))
            .withColor(ColorLib.PINK.rgb)
            .withUnderlined(true)
    }

/**
 * Creates a component from a label and value pair.
 *
 * Example: `label("Ability", "Intimidate")` -> "Ability: Intimidate"
 */
fun labelled(
    label: Any,
    value: Any?,
): MutableComponent {
    val labelComp = (label as? Component)?.copy() ?: Component.literal(label.toString())
    val styledLabelComp = labelComp.withStyle { style ->
        style.withColor(ColorLib.LIGHT_GRAY.rgb).withBold(false)
    }

    val valueComp =
        value?.let {
            when (it) {
                is Component -> it
                else -> createComponent(it.toString(), ColorLib.WHITE.rgb)
            }
        } ?: UNKNOWN

    return styledLabelComp.append(createComponent(": ", ColorLib.LIGHT_GRAY.rgb)).append(valueComp)
}

/**
 * Joins items into a single component with separators.
 *
 * Example: `join(list, ", ") { item -> createComponent(item, ColorLib.RED.rgb) }`
 */
fun <E> join(
    items: Iterable<E>?,
    separator: String,
    mapper: (E) -> Component?,
): Component {
    val parts = items?.mapNotNull(mapper)?.takeIf { it.isNotEmpty() } ?: return UNKNOWN
    val sep = createComponent(separator, ColorLib.DARK_GRAY.rgb)
    val result = Component.empty()
    parts.forEachIndexed { i, comp ->
        if (i > 0) result.append(sep)
        result.append(comp)
    }
    return result
}
