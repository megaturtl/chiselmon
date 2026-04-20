package cc.turtl.chiselmon.client.api.calc.capture

import cc.turtl.chiselmon.client.BattleState
import cc.turtl.chiselmon.client.api.ClientSpeciesRegistry
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokeball.PokeBall
import com.cobblemon.mod.common.pokemon.status.PersistentStatus
import com.cobblemon.mod.common.pokemon.status.statuses.persistent.*
import net.minecraft.client.Minecraft
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.pow

private const val MAX_LEVEL = 100f
private const val LOW_LEVEL_THRESHOLD = 13

fun estimateCaptureProbability(targetEntity: PokemonEntity, ball: PokeBall?): Float {
    val player = Minecraft.getInstance().player ?: return 0f
    val pokemon = targetEntity.pokemon
    val battle = CobblemonClient.battle

    val ctx = CaptureContext(
        targetEntity = targetEntity,
        pokemon = pokemon,
        playerActiveBattlePokemon = battle?.getParticipatingActor(player.uuid)
            ?.activePokemon?.mapNotNull { it.battlePokemon } ?: emptyList(),
        targetStatus = battle?.wildActor?.activePokemon?.firstOrNull()?.battlePokemon?.status,
        level = targetEntity.level(),
        pos = targetEntity.blockPosition(),
        turn = BattleState.currentTurn
    )

    val ballBonus = calculateBallBonus(ball, ctx)
    if (ballBonus >= 999f) return 1f

    val params = CaptureParams(
        maxHp = targetEntity.maxHealth,
        currentHp = targetEntity.health,
        catchRate = ClientSpeciesRegistry.getSpecies(pokemon.species.name)?.catchRate?.toFloat() ?: 0f,
        targetLevel = pokemon.level,
        statusMultiplier = statusMultiplier(ctx.targetStatus),
        inBattleModifier = if (targetEntity.battleId != null) 1f else 0.5f,
        levelBonus = levelBonus(pokemon.level),
        ballBonus = ballBonus
    )

    val maxPartyLevel = CobblemonClient.storage.party.slots
        .filterNotNull().maxOfOrNull { it.level }
    val difficulty = if (maxPartyLevel == null || maxPartyLevel >= pokemon.level) 1f else
        (1f - (pokemon.level - maxPartyLevel) / (MAX_LEVEL / 2f)).coerceIn(0.1f, 1f)

    return calculateCapture(params, difficulty)
}

/** Core catch rate formula: (3H - 2h) * D * C * I * B / (3H) * S * Z */
fun calculateCapture(params: CaptureParams, difficultyModifier: Float): Float {
    val rate = ((3f * params.maxHp - 2f * params.clampedHp) / (3f * params.maxHp) *
            params.darkGrassModifier * params.catchRate * params.inBattleModifier *
            params.ballBonus * params.statusMultiplier * params.levelBonus) * difficultyModifier

    val crit = min(rate * params.pokedexMultiplier / 12f, 255f) / 256f
    val shake = floor(65536.0 / (255.0 / rate.coerceIn(1f, 255f)).pow(0.1875)).toInt() / 65536f

    return (shake.toDouble().pow(4).toFloat() * (1f - crit) + shake * crit).coerceIn(0f, 1f)
}

private fun statusMultiplier(status: PersistentStatus?): Float = when (status) {
    is SleepStatus, is FrozenStatus -> 2.5f
    is ParalysisStatus, is BurnStatus, is PoisonStatus, is PoisonBadlyStatus -> 1.5f
    else -> 1f
}

private fun levelBonus(level: Int): Float =
    if (level < LOW_LEVEL_THRESHOLD) ((36f - 2f * level) / 10f).coerceAtLeast(1f) else 1f