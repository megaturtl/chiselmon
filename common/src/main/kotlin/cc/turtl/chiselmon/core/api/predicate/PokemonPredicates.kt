package cc.turtl.chiselmon.core.api.predicate

import cc.turtl.chiselmon.client.api.ClientSpeciesRegistry
import cc.turtl.chiselmon.client.config.ChiselmonConfig.general
import cc.turtl.chiselmon.core.api.calc.getPossibleMoves
import cc.turtl.chiselmon.client.feature.eggspy.EggDummy
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.properties.HiddenAbilityProperty
import java.util.function.Predicate
import cc.turtl.chiselmon.core.api.calc.*

// Simple predicates
@JvmField
val IS_SHINY: Predicate<Pokemon> = Predicate { it.shiny }
@JvmField
val IS_RIDEABLE: Predicate<Pokemon> = Predicate { it.riding.behaviours != null }
@JvmField
val IS_SHOULDERABLE: Predicate<Pokemon> = Predicate {
    ClientSpeciesRegistry.getSpecies(it.species.name)?.shoulderMountable ?: false
}
@JvmField
val IS_MARKED: Predicate<Pokemon> = Predicate { it.marks.isNotEmpty() }
@JvmField
val IS_LEGENDARY: Predicate<Pokemon> = hasAnyLabel("legendary", "mythical", "ultra_beast")
@JvmField
val IS_ULTRABEAST: Predicate<Pokemon> = hasAnyLabel("ultra_beast")
@JvmField
val IS_EGG: Predicate<Pokemon> = Predicate { it.species.resourceIdentifier == EggDummy.EGG_SPECIES_ID }
@JvmField
val IS_EGG_DUMMY: Predicate<Pokemon> = Predicate { it.forcedAspects.contains(EggDummy.DUMMY_ASPECT) }

// Cobblemon treats Pokemon with a single ability as having HA so we need to check this first
@JvmField
val HAS_HIDDEN_ABILITY: Predicate<Pokemon> = Predicate {
    countUniqueAbilities(it) > 1 && HiddenAbilityProperty(true).matches(it)
}

@JvmField
val HAS_SELF_DAMAGING_MOVE: Predicate<Pokemon> = Predicate {
    getPossibleMoves(it, true).any(IS_SELF_DAMAGING::test)
}

// Config-dependent predicates (lazily fetch config when evaluated)
@JvmField
val HAS_HIGH_IVS: Predicate<Pokemon> = Predicate { countPerfectIVs(it) >= general.thresholds.maxIvs }
@JvmField
val IS_EXTREME_SMALL: Predicate<Pokemon> = Predicate { it.scaleModifier <= general.thresholds.extremeSmall }
@JvmField
val IS_EXTREME_LARGE: Predicate<Pokemon> = Predicate { it.scaleModifier >= general.thresholds.extremeLarge }
@JvmField
val IS_EXTREME_SIZE: Predicate<Pokemon> = IS_EXTREME_SMALL.or(IS_EXTREME_LARGE)

private fun hasAnyLabel(vararg labels: String): Predicate<Pokemon> = Predicate {
    val species = ClientSpeciesRegistry.getSpecies(it.species.name) ?: return@Predicate false
    labels.any { label -> species.labels.contains(label) }
}