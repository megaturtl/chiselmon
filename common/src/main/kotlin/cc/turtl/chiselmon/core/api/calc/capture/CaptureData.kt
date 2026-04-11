package cc.turtl.chiselmon.core.api.calc.capture

import com.cobblemon.mod.common.client.battle.ClientBattlePokemon
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.status.PersistentStatus
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level

data class CaptureContext(
    val targetEntity: PokemonEntity,
    val pokemon: Pokemon,
    val playerActiveBattlePokemon: List<ClientBattlePokemon>,
    val targetStatus: PersistentStatus?,
    val level: Level,
    val pos: BlockPos
)

data class CaptureParams(
    val maxHp: Float,
    val currentHp: Float,
    val catchRate: Float,
    val targetLevel: Int,
    val statusMultiplier: Float = 1f,
    val inBattleModifier: Float = 1f,
    val darkGrassModifier: Float = 1f,
    val levelBonus: Float = 1f,
    val ballBonus: Float = 1f,
    val pokedexMultiplier: Float = 1f
) {
    val clampedHp: Float get() = currentHp.coerceIn(0f, maxHp)
}