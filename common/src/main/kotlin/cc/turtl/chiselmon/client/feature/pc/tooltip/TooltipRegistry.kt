package cc.turtl.chiselmon.client.feature.pc.tooltip

import cc.turtl.chiselmon.client.config.category.PCConfig
import cc.turtl.chiselmon.core.api.predicate.IS_EGG_DUMMY
import cc.turtl.chiselmon.core.api.predicate.IS_MARKED
import cc.turtl.chiselmon.core.api.predicate.IS_RIDEABLE
import cc.turtl.chiselmon.core.util.format.PokemonFormats
import com.cobblemon.mod.common.pokemon.Pokemon

object TooltipRegistry {
    private val entries = listOf(
        entry("ivs", { it.ivs }, { true }, PokemonFormats::ivsSummary),
        entry("original_trainer", { it.originalTrainer }, { true }, { it.originalTrainerName ?: "???" }),
        entry("form", { it.form }, { true }, { it.form.name }),
        entry("friendship", { it.friendship }, { true }, { it.friendship }),
        entry("ride_styles", { it.rideStyles }, IS_RIDEABLE::test, PokemonFormats::rideStyles),
        entry("marks", { it.marks }, IS_MARKED::test, PokemonFormats::marks),
        entry("hatch_progress", { it.hatchProgress }, IS_EGG_DUMMY::test, PokemonFormats::hatchProgress),
    )

    @JvmStatic
    fun getEntries(): List<TooltipEntry> = entries

    private fun entry(
        key: String,
        configCheck: (PCConfig.TooltipConfig) -> Boolean,
        pokemonCheck: (Pokemon) -> Boolean,
        provider: (Pokemon) -> Any
    ) = TooltipEntry(key, configCheck, pokemonCheck, provider)
}