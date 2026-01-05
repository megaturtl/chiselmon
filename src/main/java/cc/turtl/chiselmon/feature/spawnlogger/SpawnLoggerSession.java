package cc.turtl.chiselmon.feature.spawnlogger;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.chiselmon.Chiselmon;
import cc.turtl.chiselmon.api.predicate.PokemonPredicates;

public class SpawnLoggerSession {
    private long accumulatedTimeMs = 0;
    private long lastStartTime;
    private boolean paused = false;

    private final Map<UUID, LoggedPokemon> loggedPokemon = new LinkedHashMap<>();

    public SpawnLoggerSession() {
        this.lastStartTime = System.currentTimeMillis();
    }

    public void log(UUID uuid, Pokemon pokemon) {
        if (!paused) {

            LoggedPokemon data = new LoggedPokemon(
                    pokemon.getSpecies().getName(),
                    pokemon.getLevel(),
                    PokemonPredicates.IS_SHINY.test(pokemon),
                    PokemonPredicates.IS_LEGENDARY.test(pokemon),
                    PokemonPredicates.IS_EXTREME_SIZE.test(pokemon),
                    pokemon.getScaleModifier(),
                    System.currentTimeMillis());

            Chiselmon.getLogger().debug("Logging Lvl. " + pokemon.getLevel() + " " + pokemon.getSpecies().getName());
            loggedPokemon.putIfAbsent(uuid, data);
        }
    }

    public void pause() {
        if (!paused) {
            accumulatedTimeMs += (System.currentTimeMillis() - lastStartTime);
            paused = true;
        }
    }

    public void resume() {
        if (paused) {
            lastStartTime = System.currentTimeMillis();
            paused = false;
        }
    }

    public long getElapsedMs() {
        long currentChunk = paused ? 0 : (System.currentTimeMillis() - lastStartTime);
        return accumulatedTimeMs + currentChunk;
    }

    public boolean isPaused() {
        return paused;
    }

    public Collection<LoggedPokemon> getResults() {
        return loggedPokemon.values();
    }

    public int getLoggedAmount() {
        return loggedPokemon.size();
    }
}