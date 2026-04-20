package cc.turtl.chiselmon.core.api

import com.cobblemon.mod.common.pokemon.Pokemon

object PokemonComparators {
    val SIZE_COMPARATOR: Comparator<Pokemon> = compareBy { it.scaleModifier }
    val IVS_COMPARATOR: Comparator<Pokemon> = compareBy { it.ivs.getEffectiveBattleTotal() }
    val LEVEL_COMPARATOR: Comparator<Pokemon> = compareBy { it.level }
    val POKEDEX_COMPARATOR: Comparator<Pokemon> = compareBy { it.species.nationalPokedexNumber }
}
