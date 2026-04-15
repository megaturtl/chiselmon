package cc.turtl.chiselmon.core.api

import com.cobblemon.mod.common.pokemon.Pokemon

object PokemonComparators {

    @JvmField
    val SIZE_COMPARATOR: Comparator<Pokemon?> = Comparator
        .comparingDouble { p: Pokemon -> p.scaleModifier.toDouble() }

    @JvmField
    val IVS_COMPARATOR: Comparator<Pokemon?> = Comparator
        .comparingInt { p: Pokemon -> p.ivs.getEffectiveBattleTotal() }

    @JvmField
    val LEVEL_COMPARATOR: Comparator<Pokemon?> = Comparator
        .comparingInt { p: Pokemon -> p.level }

    @JvmField
    val POKEDEX_COMPARATOR: Comparator<Pokemon?> = Comparator
        .comparingInt { p: Pokemon -> p.species.nationalPokedexNumber }
}