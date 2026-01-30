package cc.turtl.chiselmon.compat.jade;

import cc.turtl.chiselmon.api.data.species.ClientSpecies;
import cc.turtl.chiselmon.api.data.species.ClientSpeciesRegistry;
import cc.turtl.chiselmon.api.predicate.PokemonPredicates;
import cc.turtl.chiselmon.util.format.ColorUtils;
import cc.turtl.chiselmon.util.format.ComponentUtils;
import cc.turtl.chiselmon.util.format.PokemonFormats;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.item.PokeBallItem;
import com.cobblemon.mod.common.pokeball.PokeBall;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
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

import java.awt.*;
import java.util.Optional;

import static cc.turtl.chiselmon.util.MiscUtil.modResource;

/**
 * Jade tooltip provider for Pokemon entities.
 * Displays customizable information based on config settings.
 */
public enum PokemonProvider implements IEntityComponentProvider {
    INSTANCE;

    // Configuration IDs
    public static final ResourceLocation POKEDEX_STATUS = modResource("pokemon_entity.pokedex_status");
    public static final ResourceLocation TYPING = modResource("pokemon_entity.typing");
    public static final ResourceLocation EFFECTIVE_TYPING = modResource("pokemon_entity.effective_typing");
    public static final ResourceLocation FORM = modResource("pokemon_entity.form");
    public static final ResourceLocation EGG_GROUPS = modResource("pokemon_entity.egg_groups");
    public static final ResourceLocation EV_YIELD = modResource("pokemon_entity.ev_yield");
    public static final ResourceLocation CATCH_RATE = modResource("pokemon_entity.catch_rate");
    public static final ResourceLocation SELF_DAMAGE_WARNING = modResource("pokemon_entity.self_damage_warning");

    private static final ResourceLocation UID = modResource("pokemon_entity");

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        if (!(accessor.getEntity() instanceof PokemonEntity pokemonEntity)) {
            return;
        }

        Pokemon pokemon = pokemonEntity.getPokemon();
        Species species = pokemon.getSpecies();
        ClientSpecies clientSpecies = ClientSpeciesRegistry.get(species.getName());

        tooltip.clear();
        addBasicInfo(tooltip, pokemon, pokemonEntity);
        addConfigurableInfo(tooltip, config, pokemon, species, clientSpecies);
        addCatchInfo(tooltip, config, pokemonEntity, accessor.getPlayer());
        addWarnings(tooltip, config, pokemon);
    }

    private void addBasicInfo(ITooltip tooltip, Pokemon pokemon, PokemonEntity entity) {
        tooltip.add(PokemonFormats.detailedName(pokemon));
        tooltip.add(new HealthElement(entity.getMaxHealth(), entity.getHealth()));
    }

    private void addConfigurableInfo(ITooltip tooltip, IPluginConfig config,
                                     Pokemon pokemon, Species species, ClientSpecies clientSpecies) {

        addIfEnabled(tooltip, config, POKEDEX_STATUS,
                "ui.label.pokedex_status",
                PokemonFormats.dexStatus(species));

        addIfEnabled(tooltip, config, TYPING,
                "ui.label.type",
                PokemonFormats.types(pokemon));

        addIfEnabled(tooltip, config, EFFECTIVE_TYPING,
                "ui.label.effective_types",
                PokemonFormats.effectiveTypesAgainst(pokemon));

        addIfEnabled(tooltip, config, FORM,
                "ui.label.form",
                PokemonFormats.form(pokemon));

        addIfEnabled(tooltip, config, EGG_GROUPS,
                "ui.label.egg_groups",
                PokemonFormats.eggGroups(clientSpecies));

        addIfEnabled(tooltip, config, EV_YIELD,
                "ui.label.ev_yield",
                PokemonFormats.evYield(clientSpecies));
    }

    private void addCatchInfo(ITooltip tooltip, IPluginConfig config,
                              PokemonEntity entity, Player player) {
        if (!config.get(CATCH_RATE)) {
            return;
        }

        ClientSpecies clientSpecies = ClientSpeciesRegistry.get(entity.getPokemon().getSpecies().getName());
        MutableComponent catchRateLabel = ComponentUtils.modTranslatable("ui.label.catch_rate");
        tooltip.add(ComponentUtils.labelled(catchRateLabel, PokemonFormats.catchRate(clientSpecies)));

        // Add catch chance if holding a pokeball
        findHeldPokeball(player).ifPresent(pokeball -> {
            tooltip.append(ComponentUtils.SPACE);
            tooltip.append(PokemonFormats.catchChance(entity, pokeball));
        });
    }

    private void addWarnings(ITooltip tooltip, IPluginConfig config, Pokemon pokemon) {
        if (!config.get(SELF_DAMAGE_WARNING)) {
            return;
        }

        if (PokemonPredicates.HAS_SELF_DAMAGING_MOVE.test(pokemon)) {
            tooltip.add(Component.literal("⚠ ").withColor(ColorUtils.RED));
            tooltip.append(PokemonFormats.selfDamagingMoves(pokemon));
        }
    }

    private void addIfEnabled(ITooltip tooltip, IPluginConfig config,
                              ResourceLocation configKey, String labelKey, Component value) {
        if (config.get(configKey)) {
            MutableComponent label = ComponentUtils.modTranslatable(labelKey);
            tooltip.add(ComponentUtils.labelled(label, value));
        }
    }

    private Optional<PokeBall> findHeldPokeball(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof PokeBallItem pokeball) {
            return Optional.of(pokeball.getPokeBall());
        }

        ItemStack offHand = player.getOffhandItem();
        if (offHand.getItem() instanceof PokeBallItem pokeball) {
            return Optional.of(pokeball.getPokeBall());
        }

        return Optional.empty();
    }
}