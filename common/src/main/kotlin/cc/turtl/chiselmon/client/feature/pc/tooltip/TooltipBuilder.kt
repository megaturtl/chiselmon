package cc.turtl.chiselmon.client.feature.pc.tooltip

import cc.turtl.chiselmon.client.config.category.PCConfig
import cc.turtl.chiselmon.core.util.format.PokemonFormats.detailedName
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.network.chat.Component

object TooltipBuilder {
    @JvmStatic
    fun build(pokemon: Pokemon, config: PCConfig.TooltipConfig, shiftDown: Boolean): Tooltip {
        val content = detailedName(pokemon, false).copy()

        if (shiftDown) {
            TooltipRegistry.getEntries()
                .filter { it.shouldDisplay(config, pokemon) }
                .forEach { content.append(Component.literal("\n")).append(it.getComponent(pokemon)) }
        }

        return Tooltip.create(content)
    }
}