package cc.turtl.chiselmon.client.feature.pc.icon

import cc.turtl.chiselmon.client.config.category.PCConfig.IconConfig
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.resources.ResourceLocation
import java.util.function.Predicate

data class IconEntry(
    val resource: ResourceLocation,
    val configCheck: Predicate<IconConfig>,
    val pokemonCheck: Predicate<Pokemon>
) {
    fun shouldDisplay(config: IconConfig, pokemon: Pokemon): Boolean {
        return configCheck.test(config) && pokemonCheck.test(pokemon)
    }
}