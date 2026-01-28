package cc.turtl.chiselmon.jade;

import com.cobblemon.mod.common.block.PokeSnackBlock;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

import static cc.turtl.chiselmon.ChiselmonConstants.MOD_ID;

@WailaPlugin(MOD_ID)
public class ChiselmonJadePlugin implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registerPokemonEntity(registration);
        registerPokeSnackBlock(registration);
    }

    private void registerPokemonEntity(IWailaClientRegistration registration) {
        registration.registerEntityComponent(PokemonProvider.INSTANCE, PokemonEntity.class);

        // Register and mark all config options
        configureOption(registration, PokemonProvider.POKEDEX_STATUS, false);
        configureOption(registration, PokemonProvider.TYPING, true);
        configureOption(registration, PokemonProvider.EFFECTIVE_TYPING, true);
        configureOption(registration, PokemonProvider.FORM, true);
        configureOption(registration, PokemonProvider.EGG_GROUPS, false);
        configureOption(registration, PokemonProvider.EV_YIELD, false);
        configureOption(registration, PokemonProvider.CATCH_RATE, true);
        configureOption(registration, PokemonProvider.SELF_DAMAGE_WARNING, true);
    }

    private void registerPokeSnackBlock(IWailaClientRegistration registration) {
        registration.registerBlockComponent(PokeSnackProvider.INSTANCE, PokeSnackBlock.class);

        configureOption(registration, PokeSnackProvider.BITES, true);
        configureOption(registration, PokeSnackProvider.INGREDIENTS, true);
        configureOption(registration, PokeSnackProvider.EFFECTS, true);
    }

    /**
     * Helper to register a config option with default value and mark as client feature.
     */
    private void configureOption(IWailaClientRegistration registration,
                                 ResourceLocation id, boolean defaultValue) {
        registration.addConfig(id, defaultValue);
        registration.markAsClientFeature(id);
    }
}