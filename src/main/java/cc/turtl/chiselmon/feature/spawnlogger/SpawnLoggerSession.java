package cc.turtl.chiselmon.feature.spawnlogger;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
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

    public void log(UUID uuid, PokemonEntity pokemonEntity) {
        if (!paused) {
            Pokemon pokemon = pokemonEntity.getPokemon();

            int x = pokemonEntity.getBlockX();
            int y = pokemonEntity.getBlockY();
            int z = pokemonEntity.getBlockZ();

            String dimensionName = pokemonEntity.level().dimension().location().toString();
            String biomeName = getBiomeName(pokemonEntity);

            LoggedPokemon data = new LoggedPokemon(
                    pokemon.getSpecies().getName(),
                    pokemon.getLevel(),
                    PokemonPredicates.IS_SHINY.test(pokemon),
                    PokemonPredicates.IS_LEGENDARY.test(pokemon),
                    PokemonPredicates.IS_EXTREME_SIZE.test(pokemon),
                    pokemon.getScaleModifier(),
                    x,
                    y,
                    z,
                    dimensionName,
                    biomeName,
                    System.currentTimeMillis());

            Chiselmon.getLogger().debug("Logging Lvl. " + pokemon.getLevel() + " " + pokemon.getSpecies().getName()
                    + " at " + String.format("%.0f, %.0f, %.0f", x, y, z) + " in " + biomeName);
            loggedPokemon.putIfAbsent(uuid, data);
        }
    }

    private String getBiomeName(PokemonEntity entity) {
        return entity.level().getBiome(entity.blockPosition())
                .unwrapKey()
                .map(key -> key.location().toString()) // Converts to "modid:biome_name"
                .orElse("Unknown");
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