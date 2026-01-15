package cc.turtl.chiselmon.feature.spawnalert.experimental;

import java.util.UUID;
import java.util.function.Supplier;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.chiselmon.api.predicate.PokemonEntityPredicates;
import cc.turtl.chiselmon.api.predicate.PokemonPredicates;
import cc.turtl.chiselmon.config.SpawnAlertConfig;
import cc.turtl.chiselmon.feature.spawnalert.AlertPriority;
import net.minecraft.network.chat.Component;

/**
 * Example “systems + ports” refactor for spawn alerts.
 * Logic stays here; side effects are provided by ports.
 */
public final class SpawnAlertSystem {
    public interface AlertSoundPort {
        void play(AlertPriority priority);
    }

    public interface ChatPort {
        void send(Component message);
    }

    public interface TimePort {
        long nowTicks();
    }

    public record EntitySpawned(UUID id, PokemonEntity pokemonEntity) {
    }

    private final SpawnAlertState state;
    private final Supplier<SpawnAlertConfig> config;
    private final AlertSoundPort sound;
    private final ChatPort chat;
    private final TimePort clock;

    public SpawnAlertSystem(
            SpawnAlertState state,
            Supplier<SpawnAlertConfig> config,
            AlertSoundPort sound,
            ChatPort chat,
            TimePort clock) {
        this.state = state;
        this.config = config;
        this.sound = sound;
        this.chat = chat;
        this.clock = clock;
    }

    public void onEntitySpawn(EntitySpawned evt) {
        onEntitySpawn(evt, config.get());
    }

    public void onEntitySpawn(EntitySpawned evt, SpawnAlertConfig cfg) {
        if (cfg == null || !cfg.enabled) {
            return;
        }
        AlertPriority priority = getAlertPriority(evt.pokemonEntity(), cfg);
        if (priority != AlertPriority.NONE) {
            state.putTarget(evt.id(), priority);
        }
    }

    public void onBattleStarted(UUID actorId) {
        if (actorId == null) {
            return;
        }
        state.removeTarget(actorId);
    }

    public void onMute(Component mutedMessage) {
        state.setMutedAll(true);
        chat.send(mutedMessage);
    }

    public void tick() {
        if (state.isMutedAll()) {
            return;
        }
        long now = clock.nowTicks();
        if (state.getLastSoundTick() == now) {
            return;
        }
        AlertPriority highest = state.highestPriority();
        if (highest != AlertPriority.NONE) {
            sound.play(highest);
        }
        state.setLastSoundTick(now);
    }

    private AlertPriority getAlertPriority(PokemonEntity pokemonEntity, SpawnAlertConfig cfg) {
        if (!PokemonEntityPredicates.IS_WILD.test(pokemonEntity)) {
            return AlertPriority.NONE;
        }

        Pokemon pokemon = pokemonEntity.getPokemon();

        if (cfg.suppressPlushies && pokemon.getLevel() == 1) {
            return AlertPriority.NONE;
        }

        boolean allowed = isAllowedAgainstBlacklist(pokemon, cfg);

        if (cfg.alertOnShiny && PokemonPredicates.IS_SHINY.test(pokemon)) {
            return AlertPriority.SHINY;
        }

        if (cfg.alertOnExtremeSize && PokemonPredicates.IS_EXTREME_SIZE.test(pokemon)) {
            return AlertPriority.SIZE;
        }

        boolean isLegendary = PokemonPredicates.IS_LEGENDARY.test(pokemon) || PokemonPredicates.IS_MYTHICAL.test(pokemon);
        if (cfg.alertOnLegendary && isLegendary && allowed) {
            return AlertPriority.LEGENDARY;
        }

        if (cfg.alertOnUltraBeast && PokemonPredicates.IS_ULTRABEAST.test(pokemon) && allowed) {
            return AlertPriority.LEGENDARY;
        }

        if (cfg.alertOnParadox && PokemonPredicates.IS_PARADOX.test(pokemon) && allowed) {
            return AlertPriority.LEGENDARY;
        }

        boolean whitelisted = cfg.alertOnCustomList && PokemonPredicates.isInCustomList(cfg.whitelist).test(pokemon);
        boolean blacklisted = PokemonPredicates.isInCustomList(cfg.blacklist).test(pokemon);
        if (whitelisted && !blacklisted) {
            return AlertPriority.CUSTOM;
        }

        return AlertPriority.NONE;
    }

    private boolean isAllowedAgainstBlacklist(Pokemon pokemon, SpawnAlertConfig cfg) {
        return !PokemonPredicates.isInCustomList(cfg.blacklist).test(pokemon);
    }
}
