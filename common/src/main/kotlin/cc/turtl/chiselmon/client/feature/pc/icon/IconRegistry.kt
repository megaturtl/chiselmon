package cc.turtl.chiselmon.client.feature.pc.icon

import cc.turtl.chiselmon.client.config.category.PCConfig.IconConfig
import cc.turtl.chiselmon.core.api.predicate.*
import cc.turtl.chiselmon.core.util.modResource
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.resources.ResourceLocation
import java.util.function.Predicate

data class IconEntry(
    val resource: ResourceLocation,
    val configCheck: Predicate<IconConfig>,
    val pokemonCheck: Predicate<Pokemon>
) {
    fun shouldDisplay(config: IconConfig, pokemon: Pokemon): Boolean =
        configCheck.test(config) && pokemonCheck.test(pokemon)
}

object IconRegistry {
    val entries: MutableList<IconEntry> = ArrayList()

    init {
        add("hidden_ability", IconConfig::hiddenAbility, HAS_HIDDEN_ABILITY)
        add("ivs", IconConfig::ivs, HAS_HIGH_IVS)
        add("shiny", IconConfig::shiny, IS_SHINY)
        add("size", IconConfig::size, IS_EXTREME_SIZE)
        add("mark", IconConfig::mark, IS_MARKED)
        add("rideable", IconConfig::rideable, IS_RIDEABLE)
        add("shoulderable", IconConfig::shoulderable, IS_SHOULDERABLE)
    }

    private fun add(path: String, cfg: Predicate<IconConfig>, pkmn: Predicate<Pokemon>) {
        entries.add(IconEntry(modResource("textures/gui/pc/icon/icon_$path.png"), cfg, pkmn))
    }
}
