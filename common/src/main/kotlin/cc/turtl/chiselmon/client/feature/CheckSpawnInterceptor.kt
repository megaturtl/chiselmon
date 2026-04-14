package cc.turtl.chiselmon.client.feature

import cc.turtl.chiselmon.client.api.ClientSpeciesRegistry
import cc.turtl.chiselmon.client.config.ChiselmonConfig.general
import cc.turtl.chiselmon.core.util.format.PokemonFormats
import cc.turtl.chiselmon.core.util.format.UNKNOWN
import cc.turtl.chiselmon.core.util.format.labelled
import cc.turtl.turtlshell.api.client.ClientEvents.COMMAND_SENT
import cc.turtl.turtlshell.api.client.ClientEvents.MESSAGE_RECEIVED
import cc.turtl.turtlshell.api.core.format.ColorLib
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style
import java.util.regex.Pattern

object CheckSpawnInterceptor {
    private val ENTRY_PATTERN: Pattern = Pattern.compile(
        "([A-Z][\\p{L}0-9\\s.\\-']+):\\s*([\\d.]+%)[,;]?"
    )

    private const val WATCH_WINDOW = 3
    private var messagesRemaining = 0

    fun init() {
        COMMAND_SENT.subscribe { e: String ->
            val config = general
            if (!config.modDisabled && config.checkSpawnDetail && e.startsWith("checkspawn")) {
                messagesRemaining = WATCH_WINDOW
            }
        }

        MESSAGE_RECEIVED.subscribe { message: Component ->
            val config = general
            if (!config.modDisabled && config.checkSpawnDetail) {
                val intercepted = tryIntercept(message)
                if (intercepted != null) {
                    // send the modified message manually, cancel the original
                    Minecraft.getInstance().gui.chat.addMessage(intercepted)
                    return@subscribe true
                }
            }
            false
        }
    }

    private fun tryIntercept(original: Component): Component? {
        if (messagesRemaining <= 0) return null

        val raw = original.string
        val matcher = ENTRY_PATTERN.matcher(raw)

        if (!matcher.find()) {
            messagesRemaining--
            return null
        }
        matcher.reset()
        messagesRemaining--

        val result = Component.empty()
        var lastEnd = 0

        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                result.append(Component.literal(raw.substring(lastEnd, matcher.start())))
            }
            result.append(buildEntry(matcher.group(1), matcher.group(2)))
            lastEnd = matcher.end()
        }

        if (lastEnd < raw.length) {
            result.append(Component.literal(raw.substring(lastEnd)))
        }

        return result
    }

    private fun buildEntry(speciesName: String, percentage: String): Component {
        val species = ClientSpeciesRegistry.getSpecies(speciesName)

        val hover = Component.empty()
            .append(Component.literal("$speciesName: "))
            .append(
                Component.literal(percentage).withColor(percentageColor(percentage))
                    .append(Component.literal("\n"))
                    .append(
                        labelled(
                            Component.translatable("chiselmon.ui.label.ev_yield"),
                            if (species == null) UNKNOWN
                                else PokemonFormats.evYield(species)
                        )
                    )
                    .append(Component.literal("\n"))
                    .append(
                        labelled(
                            Component.translatable("chiselmon.ui.label.egg_groups"),
                            if (species == null) UNKNOWN
                            else PokemonFormats.eggGroups(species)
                        )
                    )
            )

        return Component.empty()
            .append(
                Component.literal("$speciesName: ")
                    .withStyle(Style.EMPTY.withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, hover)))
            )
            .append(
                Component.literal(percentage)
                    .withStyle(
                        Style.EMPTY
                            .withColor(percentageColor(percentage))
                            .withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, hover))
                    )
            )
    }

    private fun percentageColor(percentage: String): Int {
        try {
            val value = percentage.replace("%", "").toFloat()
            if (value >= 5f) return ColorLib.GREEN.rgb
            if (value >= 0.5f) return ColorLib.YELLOW.rgb
            return ColorLib.RED.rgb
        } catch (e: NumberFormatException) {
            return ColorLib.WHITE.rgb
        }
    }
}