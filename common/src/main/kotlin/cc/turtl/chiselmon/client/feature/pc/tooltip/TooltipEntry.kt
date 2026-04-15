package cc.turtl.chiselmon.client.feature.pc.tooltip

import cc.turtl.chiselmon.client.config.category.PCConfig
import cc.turtl.chiselmon.core.util.format.labelled
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.network.chat.Component

data class TooltipEntry(
    val translationKey: String,
    val configCheck: (PCConfig.TooltipConfig) -> Boolean,
    val pokemonCheck: (Pokemon) -> Boolean,
    val componentProvider: (Pokemon) -> Any
) {
    fun shouldDisplay(config: PCConfig.TooltipConfig, pokemon: Pokemon) =
        configCheck(config) && pokemonCheck(pokemon)

    fun getComponent(pokemon: Pokemon): Component =
        labelled(
            Component.translatable("chiselmon.ui.label.$translationKey"),
            componentProvider(pokemon)
        )
}