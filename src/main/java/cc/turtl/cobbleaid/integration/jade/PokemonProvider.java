package cc.turtl.cobbleaid.integration.jade;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.item.PokeBallItem;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.cobbleaid.api.format.FormatUtil;
import cc.turtl.cobbleaid.api.format.PokemonFormatUtil;
import cc.turtl.cobbleaid.api.predicate.PokemonPredicates;
import cc.turtl.cobbleaid.api.util.ColorUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.impl.ui.HealthElement;
import static cc.turtl.cobbleaid.util.MiscUtil.modResource;

public class PokemonProvider implements IEntityComponentProvider {
    private PokemonProvider() {
    }

    public static final PokemonProvider INSTANCE = new PokemonProvider();

    public static final String POKEMON_ENTITY_PARENT_PATH = "pokemon_entity";
    public static final ResourceLocation POKEMON_ENTITY_ID = modResource(
            POKEMON_ENTITY_PARENT_PATH);
    public static final ResourceLocation POKEMON_ENTITY_TYPING_ID = modResource(
            POKEMON_ENTITY_PARENT_PATH + ".typing");
    public static final ResourceLocation POKEMON_ENTITY_EGG_GROUP_ID = modResource(
            POKEMON_ENTITY_PARENT_PATH + ".egg_groups");
    public static final ResourceLocation POKEMON_ENTITY_EV_ID = modResource(
            POKEMON_ENTITY_PARENT_PATH + ".ev_yield");
    public static final ResourceLocation POKEMON_ENTITY_CATCH_RATE_ID = modResource(
            POKEMON_ENTITY_PARENT_PATH + ".catch_rate");
    public static final ResourceLocation POKEMON_ENTITY_WARNING_ID = modResource(
            POKEMON_ENTITY_PARENT_PATH + ".self_damage_warning");

    @Override
    public ResourceLocation getUid() {
        return POKEMON_ENTITY_ID;
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

        tooltip.clear();
        tooltip.add(PokemonFormatUtil.detailedPokemonName(pokemon));
        tooltip.add(new HealthElement(pokemonEntity.getMaxHealth(), pokemonEntity.getHealth()));

        if (config.get(POKEMON_ENTITY_TYPING_ID)) {
            tooltip.add(FormatUtil.labelledValue("Type: ", PokemonFormatUtil.types(pokemon)));
        }

        if (config.get(POKEMON_ENTITY_EGG_GROUP_ID)) {
            tooltip.add(FormatUtil.labelledValue("Egg Groups: ", PokemonFormatUtil.eggGroups(pokemon.getSpecies())));
        }

        if (config.get(POKEMON_ENTITY_EV_ID)) {
            tooltip.add(FormatUtil.labelledValue("EVs: ", PokemonFormatUtil.evYield(pokemon)));
        }

        if (config.get(POKEMON_ENTITY_CATCH_RATE_ID)) {
            tooltip.add(FormatUtil.labelledValue("Catch Rate: ", pokemon.getSpecies().getCatchRate()));

            if (mainHandItem.getItem() instanceof PokeBallItem pokeBallItem) {
                tooltip.append(FormatUtil.labelledValue(" ",
                        PokemonFormatUtil.catchChance(pokemonEntity, pokeBallItem.getPokeBall())));
            }
        }

        if (config.get(POKEMON_ENTITY_WARNING_ID)) {
            if (PokemonPredicates.HAS_SELF_DAMAGING_MOVE.test(pokemon)) {
                tooltip.add(Component.literal("⚠ ")
                        .withColor(ColorUtil.RED));
                tooltip.append(PokemonFormatUtil.selfDamagingMoves(pokemon));
            }
        }
    }
}