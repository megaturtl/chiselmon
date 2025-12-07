package cc.turtl.cobbleaid.integration.jade;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class PokemonProvider implements IEntityComponentProvider {

    // singleton instance
    public static final PokemonProvider INSTANCE = new PokemonProvider();

    private PokemonProvider() {
    }

    // Jade Provider ID
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("cobbleaid", "pokemon_entity");

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        if (!(accessor.getEntity() instanceof PokemonEntity)) {
            return;
        }

        PokemonEntity pokemonEntity = (PokemonEntity) accessor.getEntity();
        Pokemon pokemon = pokemonEntity.getPokemon();
        Player player = accessor.getPlayer();

        tooltip.clear();
        tooltip.add(PokemonTooltip.nameTooltip(pokemon));
        tooltip.add(PokemonTooltip.healthTooltip(pokemonEntity));
        tooltip.add(PokemonTooltip.typingTooltip(pokemon));
        tooltip.add(PokemonTooltip.eggGroupTooltip(pokemon));
        tooltip.add(PokemonTooltip.eVYieldTooltip(pokemon));
        tooltip.add(PokemonTooltip.catchRateTooltip(pokemon));
    }

    @Override
    public ResourceLocation getUid() {
        return ID;
    }
}