package cc.turtl.chiselmon.compat.jade;

import static cc.turtl.chiselmon.Chiselmon.MODID;
import static cc.turtl.chiselmon.compat.jade.PokeSnackProvider.*;
import static cc.turtl.chiselmon.compat.jade.PokemonProvider.*;

import com.cobblemon.mod.common.block.PokeSnackBlock;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin(MODID)
public class ChiselmonJadePlugin implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {

        // POKEMON ENTITY
        registration.registerEntityComponent(PokemonProvider.INSTANCE, PokemonEntity.class);

        registration.addConfig(POKEMON_ENTITY_TYPING_ID, true);
        registration.addConfig(POKEMON_ENTITY_EFFECTIVE_TYPING_ID, true);
        registration.addConfig(POKEMON_ENTITY_FORM_ID, true);
        registration.addConfig(POKEMON_ENTITY_EGG_GROUP_ID, false);
        registration.addConfig(POKEMON_ENTITY_EV_ID, false);
        registration.addConfig(POKEMON_ENTITY_CATCH_RATE_ID, true);
        registration.addConfig(POKEMON_ENTITY_WARNING_ID, true);

        registration.markAsClientFeature(POKEMON_ENTITY_TYPING_ID);
        registration.markAsClientFeature(POKEMON_ENTITY_EFFECTIVE_TYPING_ID);
        registration.markAsClientFeature(POKEMON_ENTITY_FORM_ID);
        registration.markAsClientFeature(POKEMON_ENTITY_EGG_GROUP_ID);
        registration.markAsClientFeature(POKEMON_ENTITY_EV_ID);
        registration.markAsClientFeature(POKEMON_ENTITY_CATCH_RATE_ID);
        registration.markAsClientFeature(POKEMON_ENTITY_WARNING_ID);

        // POKESNACK BLOCK
        registration.registerBlockComponent(PokeSnackProvider.INSTANCE, PokeSnackBlock.class);

        registration.addConfig(POKESNACK_BLOCK_BITES_ID, true);
        registration.addConfig(POKESNACK_BLOCK_INGREDIENTS_ID, true);
        registration.addConfig(POKESNACK_BLOCK_EFFECTS_ID, true);

        registration.markAsClientFeature(POKESNACK_BLOCK_BITES_ID);
        registration.markAsClientFeature(POKESNACK_BLOCK_INGREDIENTS_ID);
        registration.markAsClientFeature(POKESNACK_BLOCK_EFFECTS_ID);
    }
}