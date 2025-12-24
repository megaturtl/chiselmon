package cc.turtl.cobbleaid.integration.jade;

import com.cobblemon.mod.common.block.PokeSnackBlock;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import static cc.turtl.cobbleaid.CobbleAid.MODID;
import static cc.turtl.cobbleaid.integration.jade.PokemonProvider.*;
import static cc.turtl.cobbleaid.integration.jade.PokeSnackProvider.*;

@WailaPlugin(MODID)
public class CobbleAidJadePlugin implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {

        // POKEMON ENTITY
        registration.registerEntityComponent(PokemonProvider.INSTANCE, PokemonEntity.class);

        registration.addConfig(POKEMON_ENTITY_TYPING_ID, true);
        registration.addConfig(POKEMON_ENTITY_FORM_ID, true);
        registration.addConfig(POKEMON_ENTITY_EGG_GROUP_ID, false);
        registration.addConfig(POKEMON_ENTITY_EV_ID, false);
        registration.addConfig(POKEMON_ENTITY_CATCH_RATE_ID, true);
        registration.addConfig(POKEMON_ENTITY_WARNING_ID, true);

        registration.markAsClientFeature(POKEMON_ENTITY_TYPING_ID);
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