package cc.turtl.chiselmon.core.api

import cc.turtl.chiselmon.core.api.predicate.FROM_POKESNACK
import cc.turtl.chiselmon.core.api.predicate.IS_LEGENDARY
import cc.turtl.chiselmon.core.api.predicate.IS_SHINY
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.FormData
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.pokemon.Species
import net.minecraft.client.Minecraft
import net.minecraft.core.registries.BuiltInRegistries
import java.util.*

/**
 * Immutable snapshot of a single pokemon's state when first encountered.
 */
data class PokemonEncounter(
    val encounteredMs: Long,

    // Identity
    val uuid: UUID,
    val species: Species,
    val form: FormData,

    // Attributes
    val level: Int,
    val gender: Gender,
    val scale: Float,

    // Flags
    val isShiny: Boolean,
    val isLegendary: Boolean,

    // World context
    val dimension: String,
    val biome: String,
    val dayTime: Long,
    val isRaining: Boolean,
    val blockName: String,

    // Snack
    val spawnedFromSnack: Boolean,

    // Positions
    val pokemonX: Int,
    val pokemonY: Int,
    val pokemonZ: Int,
    val playerX: Int,
    val playerY: Int,
    val playerZ: Int
) {
    companion object {

        /**
         * Creates a PokemonEncounter snapshot from a live PokemonEntity.
         */
        @JvmStatic
        fun from(pe: PokemonEntity): PokemonEncounter {
            val pokemon = pe.pokemon
            val player = Minecraft.getInstance().player
            val blockState = pe.blockStateOn

            val playerX = player?.blockX ?: 0
            val playerY = player?.blockY ?: 0
            val playerZ = player?.blockZ ?: 0

            return PokemonEncounter(
                encounteredMs = System.currentTimeMillis(),

                // Identity
                uuid = pe.uuid,
                species = pokemon.species,
                form = pe.form,

                // Attributes
                level = pokemon.level,
                gender = pokemon.gender,
                scale = pokemon.scaleModifier,

                // Flags
                isShiny = IS_SHINY.test(pokemon),
                isLegendary = IS_LEGENDARY.test(pokemon),

                // World context
                dimension = pe.level().dimension().location().toString().intern(),
                biome = pe.level().getBiome(pe.blockPosition()).registeredName.intern(),
                dayTime = pe.level().dayTime % 24000,
                isRaining = pe.level().isRaining,
                blockName = BuiltInRegistries.BLOCK.getKey(blockState.block).toString(),

                // Snack
                spawnedFromSnack = FROM_POKESNACK.test(pe),

                // Positions
                pokemonX = pe.blockX,
                pokemonY = pe.blockY,
                pokemonZ = pe.blockZ,
                playerX = playerX,
                playerY = playerY,
                playerZ = playerZ
            )
        }
    }
}