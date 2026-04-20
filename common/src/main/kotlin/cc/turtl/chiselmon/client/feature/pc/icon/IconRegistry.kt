package cc.turtl.chiselmon.client.feature.pc.icon

import cc.turtl.chiselmon.client.config.category.PCConfig.IconConfig
import cc.turtl.chiselmon.core.api.predicate.*
import cc.turtl.chiselmon.core.util.modResource
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.resources.ResourceLocation

data class IconEntry(
    val resource: ResourceLocation,
    val configCheck: (IconConfig) -> Boolean,
    val pokemonCheck: (Pokemon) -> Boolean
) {
    fun shouldDisplay(config: IconConfig, pokemon: Pokemon): Boolean =
        configCheck(config) && pokemonCheck(pokemon)
}

object IconRegistry {
    val entries: List<IconEntry> = buildList {
        add("hidden_ability", IconConfig::hiddenAbility, HAS_HIDDEN_ABILITY::test)
        add("ivs", IconConfig::ivs, HAS_HIGH_IVS::test)
        add("shiny", IconConfig::shiny, IS_SHINY::test)
        add("size", IconConfig::size, IS_EXTREME_SIZE::test)
        add("mark", IconConfig::mark, IS_MARKED::test)
        add("rideable", IconConfig::rideable, IS_RIDEABLE::test)
        add("shoulderable", IconConfig::shoulderable, IS_SHOULDERABLE::test)
    }

    private fun MutableList<IconEntry>.add(
        path: String,
        cfg: (IconConfig) -> Boolean,
        pkmn: (Pokemon) -> Boolean
    ) = add(IconEntry(modResource("textures/gui/pc/icon/icon_$path.png"), cfg, pkmn))
}
