package cc.turtl.chiselmon.compat.jade;

import static cc.turtl.chiselmon.util.TextUtil.modResource;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.item.PokeBallItem;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.chiselmon.api.data.SimpleSpecies;
import cc.turtl.chiselmon.api.data.SimpleSpeciesRegistry;
import cc.turtl.chiselmon.api.predicate.PokemonPredicates;
import cc.turtl.chiselmon.api.util.PokemonFormatUtil;
import cc.turtl.chiselmon.util.ColorUtil;
import cc.turtl.chiselmon.util.ComponentUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.impl.ui.HealthElement;

public class PokemonProvider implements IEntityComponentProvider {
    private PokemonProvider() {
    }

    public static final PokemonProvider INSTANCE = new PokemonProvider();

    public static final String POKEMON_ENTITY_PARENT_PATH = "pokemon_entity";
    public static final ResourceLocation POKEMON_ENTITY_ID = modResource(
            POKEMON_ENTITY_PARENT_PATH);
    public static final ResourceLocation POKEMON_ENTITY_TYPING_ID = modResource(
            POKEMON_ENTITY_PARENT_PATH + ".typing");
    public static final ResourceLocation POKEMON_ENTITY_EFFECTIVE_TYPING_ID = modResource(
            POKEMON_ENTITY_PARENT_PATH + ".effective_typing");
    public static final ResourceLocation POKEMON_ENTITY_FORM_ID = modResource(
            POKEMON_ENTITY_PARENT_PATH + ".form");
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
        SimpleSpecies simpleSpecies = SimpleSpeciesRegistry.getByName(pokemon.getSpecies().getName());
        Player player = accessor.getPlayer();

        ItemStack mainHandItem = player.getMainHandItem();
        ItemStack offHandItem = player.getOffhandItem();

        tooltip.clear();
        tooltip.add(PokemonFormatUtil.detailedPokemonName(pokemon));
        tooltip.add(new HealthElement(pokemonEntity.getMaxHealth(), pokemonEntity.getHealth()));

        if (config.get(POKEMON_ENTITY_TYPING_ID)) {
            MutableComponent typeLabel = ComponentUtil.modTranslatable("ui.label.type");
            tooltip.add(ComponentUtil.labelledValue(typeLabel, PokemonFormatUtil.types(pokemon)));
        }

        if (config.get(POKEMON_ENTITY_EFFECTIVE_TYPING_ID)) {
            MutableComponent effectiveTypeLabel = ComponentUtil.modTranslatable("ui.label.effective_types");
            tooltip.add(
                    ComponentUtil.labelledValue(effectiveTypeLabel, PokemonFormatUtil.effectiveTypesAgainst(pokemon)));
        }

        if (config.get(POKEMON_ENTITY_FORM_ID)) {
            MutableComponent formLabel = ComponentUtil.modTranslatable("ui.label.form");
            tooltip.add(ComponentUtil.labelledValue(formLabel, pokemon.getForm().getName()));
        }

        if (config.get(POKEMON_ENTITY_EGG_GROUP_ID)) {
            MutableComponent eggGroupsLabel = ComponentUtil.modTranslatable("ui.label.egg_groups");
            tooltip.add(ComponentUtil.labelledValue(eggGroupsLabel, PokemonFormatUtil.eggGroups(simpleSpecies)));
        }

        if (config.get(POKEMON_ENTITY_EV_ID)) {
            MutableComponent evYieldLabel = ComponentUtil.modTranslatable("ui.label.ev_yield");
            tooltip.add(ComponentUtil.labelledValue(evYieldLabel, PokemonFormatUtil.evYield(simpleSpecies)));
        }

        if (config.get(POKEMON_ENTITY_CATCH_RATE_ID)) {
            MutableComponent catchRateLabel = ComponentUtil.modTranslatable("ui.label.catch_rate");
            tooltip.add(ComponentUtil.labelledValue(catchRateLabel, PokemonFormatUtil.catchRate(simpleSpecies)));

            if (mainHandItem.getItem() instanceof PokeBallItem mainHandPokeball) {
                tooltip.append(ComponentUtil.labelledValue(" ",
                        PokemonFormatUtil.catchChance(pokemonEntity, mainHandPokeball.getPokeBall())));
            } else if (offHandItem.getItem() instanceof PokeBallItem offHandPokeball) {
                tooltip.append(ComponentUtil.labelledValue(" ",
                        PokemonFormatUtil.catchChance(pokemonEntity, offHandPokeball.getPokeBall())));
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