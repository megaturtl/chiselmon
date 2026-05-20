package cc.turtl.chiselmon.client.feature.eggspy

import cc.turtl.chiselmon.client.api.duck.DuckPreviewPokemon
import cc.turtl.chiselmon.core.ChiselmonConstants
import cc.turtl.chiselmon.mixin.accessor.AccessorPokemon
import com.cobblemon.mod.common.api.abilities.Ability
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.RenderablePokemon
import com.cobblemon.mod.common.util.DataKeys
import net.minecraft.client.Minecraft
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.TagParser
import net.minecraft.resources.ResourceLocation

/**
 * Represents the pokemon a NeoDaycare egg will hatch into.
 *
 * To most methods, this looks like a regular Pokemon, with a forced
 * "EggDummy" aspect for recognition by Chiselmon methods.
 */
class EggDummy private constructor(
    private val cachedRenderableEgg: RenderablePokemon
) : Pokemon() {

    var totalSteps: Int = 0
        internal set

    var hatchPercentage: Int = 0
        internal set

    val stepsRemaining: Int
        get() = totalSteps - (totalSteps * hatchPercentage / 100)

    val originalRenderablePokemon: RenderablePokemon
        get() = cachedRenderableEgg

    override fun attemptAbilityUpdate() {
        // Ability comes from hatchling data and should never be recalculated
    }

    companion object {
        const val DUMMY_ASPECT = "EggDummy"

        @JvmField
        val EGG_SPECIES_ID: ResourceLocation = ResourceLocation.fromNamespaceAndPath("neodaycare", "egg_species")
        const val HATCH_PERCENTAGE_FEATURE = "hatch_percentage"

        fun from(egg: Pokemon): EggDummy? {
            val eggData = egg.persistentData.getString("Egg")
            if (eggData.isEmpty()) return null

            return try {
                val hatchlingNbt = TagParser.parseTag(eggData)
                val registries = Minecraft.getInstance().level?.registryAccess() ?: return null
                val ops = registries.createSerializationContext(NbtOps.INSTANCE)
                val renderableEgg = (egg as DuckPreviewPokemon).`chiselmon$getRawRenderablePokemon`()

                val decoded = CODEC.decode(ops, hatchlingNbt)
                    .result()
                    .orElse(null)
                    ?.getFirst() ?: return null

                // The decoded aspects miss form-specific aspects like "hisuian" so it needs to be set manually
                val formId = hatchlingNbt.getString(DataKeys.POKEMON_FORM_ID)
                val formAspects = decoded.species.forms
                    .firstOrNull { it.formOnlyShowdownId() == formId }
                    ?.aspects
                    ?: emptySet()
                val capturedAspects = decoded.aspects + formAspects

                EggDummy(renderableEgg).also { dummy ->
                    dummy.copyFrom(decoded)

                    // The codec decode chain corrupts the ability so it needs to be reapplied.
                    Ability.CODEC.decode(NbtOps.INSTANCE, hatchlingNbt.getCompound(DataKeys.POKEMON_ABILITY))
                        .result()
                        .orElse(null)
                        ?.getFirst()
                        ?.let {
                            @Suppress("CAST_NEVER_SUCCEEDS")
                            (dummy as AccessorPokemon).`chiselmon$setAbility`(it)
                        }

                    // Lock client mode so updateAspects just uses forcedAspects (preserves the form aspects set above)
                    @Suppress("CAST_NEVER_SUCCEEDS")
                    (dummy as AccessorPokemon).`chiselmon$setIsClient`(true)
                    dummy.forcedAspects = capturedAspects + DUMMY_ASPECT
                    dummy.uuid = egg.uuid
                }
            } catch (e: Exception) {
                ChiselmonConstants.LOGGER.error("Failed to parse hatchling for egg: {}", egg.uuid, e)
                null
            }
        }
    }
}