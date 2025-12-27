package cc.turtl.cobbleaid.feature.spawnlogger;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import com.cobblemon.mod.common.pokemon.Pokemon;
import cc.turtl.cobbleaid.api.predicate.PokemonPredicates;

public class SpawnLoggerSession {
    private final long targetDurationMs;
    private long accumulatedTimeMs = 0;
    private long lastStartTime;
    private boolean paused = false;

    private final Map<UUID, LoggedPokemon> loggedPokemon = new LinkedHashMap<>();

    public SpawnLoggerSession(int durationMinutes) {
        this.targetDurationMs = (long) durationMinutes * 60 * 1000;
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

            loggedPokemon.putIfAbsent(uuid, data);
        }
    }

    public void togglePause() {
        if (paused) {
            lastStartTime = System.currentTimeMillis();
            paused = false;
        } else {
            accumulatedTimeMs += (System.currentTimeMillis() - lastStartTime);
            paused = true;
        }
    }

    public boolean isExpired() {
        if (paused)
            return false;
        return getElapsedMs() >= targetDurationMs;
    }

    private long getElapsedMs() {
        long currentChunk = paused ? 0 : (System.currentTimeMillis() - lastStartTime);
        return accumulatedTimeMs + currentChunk;
    }

    public long getElapsedSeconds() {
        return getElapsedMs() / 1000;
    }

    public long getRemainingSeconds() {
        return (targetDurationMs - getElapsedMs()) / 1000;
    }

    public boolean isPaused() {
        return paused;
    }

    public Collection<LoggedPokemon> getResults() {
        return loggedPokemon.values();
    }
}