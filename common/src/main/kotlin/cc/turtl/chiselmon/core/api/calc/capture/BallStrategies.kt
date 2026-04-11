package cc.turtl.chiselmon.core.api.calc.capture

import cc.turtl.chiselmon.client.api.ClientSpeciesRegistry
import cc.turtl.chiselmon.core.api.predicate.IS_ULTRABEAST
import com.cobblemon.mod.common.api.pokeball.PokeBalls
import com.cobblemon.mod.common.api.pokedex.PokedexEntryProgress
import com.cobblemon.mod.common.api.tags.CobblemonBiomeTags
import com.cobblemon.mod.common.api.types.ElementalTypes
import com.cobblemon.mod.common.battles.BattleRegistry
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.pokeball.PokeBall
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.pokemon.status.statuses.persistent.SleepStatus

fun interface BallStrategy {
    fun calculate(ctx: CaptureContext): Float
}

// Extension point for mod compatibility or runtime additions.
private val customStrategies = mutableMapOf<PokeBall, BallStrategy>()

fun registerBallStrategy(ball: PokeBall, strategy: BallStrategy) {
    customStrategies[ball] = strategy
}

fun calculateBallBonus(ball: PokeBall?, ctx: CaptureContext): Float {
    if (ball == null) return 1f
    customStrategies[ball]?.let { return it.calculate(ctx) }
    return builtInStrategy(ball, ctx)
}

private fun builtInStrategy(ball: PokeBall, ctx: CaptureContext): Float = when (ball) {
    // Standard balls
    PokeBalls.POKE_BALL -> 1f
    PokeBalls.GREAT_BALL -> 1.5f
    PokeBalls.SPORT_BALL -> 1.5f
    PokeBalls.ULTRA_BALL -> 2f
    PokeBalls.MASTER_BALL -> 999f

    // Ancient balls
    PokeBalls.ANCIENT_GREAT_BALL -> 1.5f
    PokeBalls.ANCIENT_ULTRA_BALL -> 2f
    PokeBalls.ANCIENT_ORIGIN_BALL -> 999f

    // Effect-only balls
    PokeBalls.FRIEND_BALL,
    PokeBalls.LUXURY_BALL,
    PokeBalls.PREMIER_BALL,
    PokeBalls.HEAL_BALL,
    PokeBalls.CHERISH_BALL -> 1f

    // Specialty balls
    PokeBalls.PARK_BALL -> if (ctx.level.getBiome(ctx.pos).`is`(CobblemonBiomeTags.IS_TEMPERATE)) 2.5f else 1f
    PokeBalls.SAFARI_BALL -> if (!ctx.targetEntity.isBattling) 1.5f else 1f
    PokeBalls.FAST_BALL -> {
        val speed = ClientSpeciesRegistry.get(ctx.pokemon.species.name)?.baseStats?.get("speed") ?: 0
        if (speed >= 100) 4f else 1f
    }

    PokeBalls.LEVEL_BALL -> levelBallBonus(ctx)
    PokeBalls.LURE_BALL -> if (ctx.pokemon.aspects.contains("fished")) 4f else 1f
    PokeBalls.HEAVY_BALL -> when {
        ctx.pokemon.form.weight >= 3000f -> 4f
        ctx.pokemon.form.weight >= 2000f -> 2.5f
        ctx.pokemon.form.weight >= 1000f -> 1.5f
        else -> 1f
    }

    PokeBalls.LOVE_BALL -> loveBallBonus(ctx)
    PokeBalls.MOON_BALL -> moonBallBonus(ctx)
    PokeBalls.NET_BALL -> if (ctx.pokemon.types.any { it == ElementalTypes.BUG || it == ElementalTypes.WATER }) 3f else 1f
    PokeBalls.DIVE_BALL -> if (ctx.targetEntity.isUnderWater) 3.5f else 1f
    PokeBalls.NEST_BALL -> ctx.pokemon.level.let { if (it < 30) (41f - it) / 10f else 1f }
    PokeBalls.REPEAT_BALL -> {
        val caught = CobblemonClient.clientPokedexData
            .getKnowledgeForSpecies(ctx.pokemon.species.resourceIdentifier) == PokedexEntryProgress.CAUGHT
        if (caught) 3.5f else 1f
    }

    PokeBalls.TIMER_BALL -> timerBallBonus(ctx)
    PokeBalls.DUSK_BALL -> {
        val brightness = ctx.level.getMaxLocalRawBrightness(ctx.pos)
        when {
            brightness == 0 -> 3.5f
            brightness <= 7 -> 3f
            else -> 1f
        }
    }

    PokeBalls.QUICK_BALL -> quickBallBonus(ctx)
    PokeBalls.DREAM_BALL -> if (ctx.targetStatus is SleepStatus) 4f else 1f

    // Cobblemon hasn't implemented the 0.1x penalty yet
    PokeBalls.BEAST_BALL -> if (IS_ULTRABEAST.test(ctx.pokemon)) 5f else 1f

    else -> 1f
}

private fun levelBallBonus(ctx: CaptureContext): Float {
    val max = ctx.playerActiveBattlePokemon.maxOfOrNull { it.level } ?: return 1f
    return when {
        max > ctx.pokemon.level * 4 -> 4f
        max > ctx.pokemon.level * 2 -> 3f
        max > ctx.pokemon.level -> 2f
        else -> 1f
    }
}

private fun loveBallBonus(ctx: CaptureContext): Float {
    if (ctx.pokemon.gender == Gender.GENDERLESS) return 1f
    val opposite = ctx.playerActiveBattlePokemon
        .filter { it.gender != Gender.GENDERLESS && it.gender != ctx.pokemon.gender }
    return when {
        opposite.any { it.species == ctx.pokemon.species } -> 8f
        opposite.isNotEmpty() -> 2.5f
        else -> 1f
    }
}

private fun moonBallBonus(ctx: CaptureContext): Float {
    if (ctx.level.dayTime % 24000 < 12000) return 1f
    return when (ctx.level.moonPhase) {
        0 -> 4f
        1, 7 -> 2.5f
        2, 6 -> 1.5f
        else -> 1f
    }
}

private fun timerBallBonus(ctx: CaptureContext): Float {
    val turn = ctx.targetEntity.battleId?.let { BattleRegistry.getBattle(it) }?.turn ?: return 1f
    return (1f + turn * (1229f / 4096f)).coerceAtMost(4f)
}

private fun quickBallBonus(ctx: CaptureContext): Float {
    val turn = ctx.targetEntity.battleId?.let { BattleRegistry.getBattle(it) }?.turn ?: return 1f
    return if (turn == 1) 5f else 1f
}