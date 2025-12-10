package cc.turtl.cobbleaid.integration.jade;

import java.util.List;

import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.item.PokeBallItem;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.cobbleaid.api.PokemonTooltip;
import cc.turtl.cobbleaid.api.component.ComponentColor;
import cc.turtl.cobbleaid.api.component.SelfDamageFormatter;
import cc.turtl.cobbleaid.api.filter.SelfDamageHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.impl.ui.HealthElement;

public class PokemonProvider implements IEntityComponentProvider {

    // singleton instance
    public static final PokemonProvider INSTANCE = new PokemonProvider();

    private PokemonProvider() {
    }

    // Jade Provider ID
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("cobbleaid", "pokemon_entity");

    @Override
    public ResourceLocation getUid() {
        return ID;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        if (!(accessor.getEntity() instanceof PokemonEntity)) {
            return;
        }

        PokemonEntity pokemonEntity = (PokemonEntity) accessor.getEntity();
        Pokemon pokemon = pokemonEntity.getPokemon();
        Player player = accessor.getPlayer();

        ItemStack mainHandItem = player.getMainHandItem();
        List<MoveTemplate> selfDamagingMoves = SelfDamageHelper.getSelfDamagingMoves(pokemon);

        tooltip.clear();
        tooltip.add(PokemonTooltip.nameTooltip(pokemon));
        tooltip.add(new HealthElement(pokemonEntity.getMaxHealth(), pokemonEntity.getHealth()));
        tooltip.add(PokemonTooltip.typingTooltip(pokemon));
        tooltip.add(PokemonTooltip.eggGroupTooltip(pokemon));
        tooltip.add(PokemonTooltip.eVYieldTooltip(pokemon));

        if (mainHandItem.getItem() instanceof PokeBallItem pokeBallItem) {
            tooltip.add(PokemonTooltip.catchChanceTooltip(pokemonEntity, pokeBallItem.getPokeBall()));
        } else {
            tooltip.add(PokemonTooltip.labeledTooltip("Catch Rate: ", pokemon.getSpecies().getCatchRate()));
        }

        if (!selfDamagingMoves.isEmpty()) {
            tooltip.add(Component.literal("Careful! Learns: ")
                    .withColor(ComponentColor.RED));
            tooltip.add(SelfDamageFormatter.format(pokemon));
        }
    }
}