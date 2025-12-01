package cc.turtl.cobbleaid.integration.jade;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.cobbleaid.api.PokemonTooltips;
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
        tooltip.add(PokemonTooltips.computeNameTooltip(pokemon));
        tooltip.add(PokemonTooltips.computeTypingTooltip(pokemon));
        tooltip.add(PokemonTooltips.computeGenderTooltip(pokemon));
        tooltip.add(PokemonTooltips.computeNatureTooltip(pokemon));
        tooltip.add(PokemonTooltips.computeSizeTooltip(pokemon));
        tooltip.add(PokemonTooltips.computeIVsTooltip(pokemon));
        tooltip.add(PokemonTooltips.computeCatchChanceTooltip(pokemonEntity, player));
    }

    @Override
    public ResourceLocation getUid() {
        return ID;
    }
}