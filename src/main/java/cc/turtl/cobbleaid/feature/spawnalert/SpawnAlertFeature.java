package cc.turtl.cobbleaid.feature.spawnalert;

import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.cobbleaid.api.predicate.PokemonEntityPredicates;
import cc.turtl.cobbleaid.api.predicate.PokemonPredicates;
import cc.turtl.cobbleaid.feature.AbstractFeature;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;

import com.cobblemon.mod.common.api.Priority;

public final class SpawnAlertFeature extends AbstractFeature {
    private static final SpawnAlertFeature INSTANCE = new SpawnAlertFeature();
    private AlertSoundManager alertSoundManager;

    private SpawnAlertFeature() {
        super("SpawnAlert");
    }

    public static SpawnAlertFeature getInstance() {
        return INSTANCE;
    }

    @Override
    protected boolean isFeatureEnabled() {
        return getConfig().spawnAlert.enabled;
    }

    @Override
    protected void init() {
        alertSoundManager = new AlertSoundManager(getConfig().spawnAlert);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (canRun()) {
                alertSoundManager.tick();
            }
        });

        ClientEntityEvents.ENTITY_LOAD.register(this::onEntityLoad);

        ClientEntityEvents.ENTITY_UNLOAD.register((entity, level) -> {
            alertSoundManager.removeTarget(entity.getUUID());
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            alertSoundManager.clearTargets();
        });

        // if battle starts, remove the pokemon from the alert list
        CobblemonEvents.BATTLE_STARTED_POST.subscribe(Priority.NORMAL, event -> {
            event.getBattle().getActors().forEach(actor -> {
                actor.getPokemonList().forEach(pokemon -> {
                    alertSoundManager.removeTarget(pokemon.getEntity().getUUID());
                });
            });
            return kotlin.Unit.INSTANCE;
        });
    }

    private void onEntityLoad(Entity entity, ClientLevel level) {
        if (canRun() && entity instanceof PokemonEntity pe) {
            if (shouldAlert(pe, getConfig().spawnAlert)) {
                alertSoundManager.addTarget(entity.getUUID());
                AlertMessage.sendChatAlert(pe);
            }
        }
    }

    private boolean shouldAlert(PokemonEntity pokemonEntity, SpawnAlertConfig config) {
        if (!PokemonEntityPredicates.IS_WILD.test(pokemonEntity))
            return false;

        Pokemon pokemon = pokemonEntity.getPokemon();

        if (PokemonPredicates.isInCustomList(config.blackList).test(pokemon)) {
            return false;
        }

        return (config.alertOnShiny && PokemonPredicates.IS_SHINY.test(pokemon))
                || (config.alertOnLegendary && (PokemonPredicates.IS_LEGENDARY.test(pokemon)
                        || PokemonPredicates.IS_MYTHICAL.test(pokemon)))
                || (config.alertOnUltraBeast && PokemonPredicates.IS_ULTRABEAST.test(pokemon))
                || (config.alertOnParadox && PokemonPredicates.IS_PARADOX.test(pokemon))
                || (config.alertOnExtremeSize && PokemonPredicates.IS_EXTREME_SIZE.test(pokemon))
                || (config.alertOnCustomList
                        && PokemonPredicates.isInCustomList(config.whiteList).test(pokemon));
    }

    public AlertSoundManager getAlertSoundManager() {
        return alertSoundManager;
    }
}