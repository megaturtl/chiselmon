package cc.turtl.cobbleaid.feature.spawnalert;

import org.apache.logging.log4j.Logger;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.api.predicate.PokemonPredicates;
import cc.turtl.cobbleaid.config.SpawnAlertConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;

public final class SpawnAlertFeature {
    private static SpawnAlertFeature INSTANCE;
    private static Logger LOGGER = CobbleAid.getLogger();

    private SpawnAlertFeature() {
    }

    public static void register() {
        INSTANCE = new SpawnAlertFeature();
        INSTANCE.registerListeners();
        LOGGER.debug("SpawnAlert Feature Registered");
    }

    private void registerListeners() {
        ClientEntityEvents.ENTITY_LOAD.register(this::onEntityLoad);
    }

    private void onEntityLoad(Entity entity, ClientLevel clientLevel) {
        if (CobbleAid.isDisabled()) return;
        if (entity instanceof PokemonEntity pe) {
            SpawnAlertConfig config = CobbleAid.services().config().get().spawnAlert;
            shouldAlert(pe, config);
        }
    }

    private boolean shouldAlert(PokemonEntity pokemonEntity, SpawnAlertConfig config) {
        Pokemon pokemon = pokemonEntity.getPokemon();
        if (config.alertOnShiny && PokemonPredicates.IS_SHINY.test(pokemon)) {
            return true;
        }

        if (config.alertOnLegendary && PokemonPredicates.IS_LEGENDARY.test(pokemon)) {
            return true;
        }

        if (config.alertOnUltraBeast && PokemonPredicates.IS_ULTRABEAST.test(pokemon)) {
            return true;
        }

        if (config.alertOnParadox && PokemonPredicates.IS_PARADOX.test(pokemon)) {
            return true;
        }

        if (config.alertOnCustomList &&
                PokemonPredicates.isInCustomList(config.customPokemonList).test(pokemon)) {
            return true;
        }

        return false;
    }
}