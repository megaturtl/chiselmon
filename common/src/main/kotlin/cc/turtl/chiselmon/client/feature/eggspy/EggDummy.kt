package cc.turtl.chiselmon.client.feature.eggspy

import cc.turtl.chiselmon.client.api.duck.DuckPreviewPokemon
import cc.turtl.chiselmon.core.ChiselmonConstants
import com.cobblemon.mod.common.api.pokemon.feature.IntSpeciesFeature
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.RenderablePokemon
import net.minecraft.client.Minecraft
import net.minecraft.nbt.TagParser
import net.minecraft.resources.ResourceLocation

/**
 * Represents the pokemon a NeoDaycare egg will hatch into.
 *
 * To most methods, this looks like a regular Pokemon, with a forced
 * "EggDummy" aspect for recognition by Chiselmon methods.
 */
class EggDummy(val originalEgg: Pokemon) : Pokemon() {

    var totalSteps: Int = 0
        private set

    val hatchPercentage: Int
        get() = originalEgg.getFeature<IntSpeciesFeature>(HATCH_PERCENTAGE_FEATURE)?.value ?: 0

    val stepsRemaining: Int
        get() = totalSteps - (totalSteps * hatchPercentage / 100)

    val originalRenderablePokemon: RenderablePokemon
        get() = (originalEgg as DuckPreviewPokemon).`chiselmon$getRawRenderablePokemon`()

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

                EggDummy(egg).also { dummy ->
                    dummy.totalSteps = egg.persistentData.getInt("TotalSteps")
                    dummy.loadFromNBT(registries, hatchlingNbt)
                    dummy.uuid = egg.uuid
                    dummy.forcedAspects += DUMMY_ASPECT
                }
            } catch (e: Exception) {
                ChiselmonConstants.LOGGER.error("Failed to parse hatchling for egg: {}", egg.uuid, e)
                null
            }
        }
    }
}