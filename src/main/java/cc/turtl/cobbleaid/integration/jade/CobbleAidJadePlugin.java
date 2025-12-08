package cc.turtl.cobbleaid.integration.jade;

import com.cobblemon.mod.common.block.PokeSnackBlock;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import cc.turtl.cobbleaid.CobbleAid;

@WailaPlugin(CobbleAid.MODID)
public class CobbleAidJadePlugin implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityComponent(
            PokemonProvider.INSTANCE, 
            PokemonEntity.class
        );
        registration.registerBlockComponent(
            PokeSnackBlockProvider.INSTANCE, 
            PokeSnackBlock.class
        );
    }
}