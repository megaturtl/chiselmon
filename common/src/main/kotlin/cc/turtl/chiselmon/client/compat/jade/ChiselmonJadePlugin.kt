package cc.turtl.chiselmon.client.compat.jade

import cc.turtl.chiselmon.BuildDetails
import com.cobblemon.mod.common.block.PokeSnackBlock
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.resources.ResourceLocation
import snownee.jade.api.IWailaClientRegistration
import snownee.jade.api.IWailaPlugin
import snownee.jade.api.WailaPlugin

@WailaPlugin(BuildDetails.MOD_ID)
class ChiselmonJadePlugin : IWailaPlugin {
    override fun registerClient(registration: IWailaClientRegistration) {
        registerPokemonEntity(registration)
        registerPokeSnackBlock(registration)
    }

    private fun registerPokemonEntity(registration: IWailaClientRegistration) {
        registration.registerEntityComponent(PokemonProvider, PokemonEntity::class.java)

        // Register and mark all config options
        configureOption(registration, PokemonProvider.POKEDEX_STATUS, false)
        configureOption(registration, PokemonProvider.TYPING, true)
        configureOption(registration, PokemonProvider.WEAKNESSES, true)
        configureOption(registration, PokemonProvider.FORM, true)
        configureOption(registration, PokemonProvider.EGG_GROUPS, false)
        configureOption(registration, PokemonProvider.EV_YIELD, false)
        configureOption(registration, PokemonProvider.CATCH_RATE, true)
        configureOption(registration, PokemonProvider.SELF_DAMAGE_WARNING, true)
    }

    private fun registerPokeSnackBlock(registration: IWailaClientRegistration) {
        registration.registerBlockComponent(PokeSnackProvider, PokeSnackBlock::class.java)

        configureOption(registration, PokeSnackProvider.BITES, true)
        configureOption(registration, PokeSnackProvider.INGREDIENTS, true)
        configureOption(registration, PokeSnackProvider.EFFECTS, true)
    }

    /**
     * Helper to register a config option with default value and mark as client feature.
     */
    private fun configureOption(
        registration: IWailaClientRegistration,
        id: ResourceLocation?, defaultValue: Boolean
    ) {
        registration.addConfig(id, defaultValue)
        registration.markAsClientFeature(id)
    }
}