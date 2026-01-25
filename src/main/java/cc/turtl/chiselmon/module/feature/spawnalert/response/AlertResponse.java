package cc.turtl.chiselmon.module.feature.spawnalert.response;

import java.util.HashSet;
import java.util.Set;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.chiselmon.api.predicate.PokemonEntityPredicates;
import cc.turtl.chiselmon.api.predicate.PokemonPredicates;
import cc.turtl.chiselmon.module.feature.spawnalert.AlertLevel;
import cc.turtl.chiselmon.module.feature.spawnalert.SpawnAlertConfig;

public record AlertResponse(
        PokemonEntity pe,
        AlertLevel glowLevel,
        AlertLevel chatLevel,
        AlertLevel soundLevel) {

    public static AlertResponse empty(PokemonEntity pe) {
        return new AlertResponse(pe, AlertLevel.NONE, AlertLevel.NONE, AlertLevel.NONE);
    }

    public static AlertResponse skipped() {
        return new AlertResponse(null, AlertLevel.NONE, AlertLevel.NONE, AlertLevel.NONE);
    }

    public boolean isEmpty() {
        return glowLevel == AlertLevel.NONE &&
                chatLevel == AlertLevel.NONE &&
                soundLevel == AlertLevel.NONE;
    }

    public static AlertResponse calculate(SpawnAlertConfig config, PokemonEntity pe, boolean muted) {
        Pokemon pokemon = pe.getPokemon();
        String name = pokemon.getSpecies().getName();

        // Skip blacklisted or ignored (non wild)
        boolean isBlacklisted = config.blacklist.stream().anyMatch(s -> s.equalsIgnoreCase(name));
        if (isBlacklisted)
            return skipped();
        if (!PokemonEntityPredicates.IS_WILD.test(pe))
            return skipped();
        if (config.suppressPlushies && pokemon.getLevel() == 1 && !pokemon.getShiny())
            return skipped();

        // Identify all applicable levels
        Set<AlertLevel> activeLevels = new HashSet<>();
        if (PokemonPredicates.IS_SHINY.test(pokemon))
            activeLevels.add(AlertLevel.SHINY);
        if (PokemonPredicates.IS_EXTREME_SIZE.test(pokemon))
            activeLevels.add(AlertLevel.SIZE);
        if (PokemonPredicates.IS_SPECIAL.test(pokemon))
            activeLevels.add(AlertLevel.LEGENDARY);

        boolean isWhitelisted = config.whitelist.stream().anyMatch(s -> s.equalsIgnoreCase(name));
        if (isWhitelisted)
            activeLevels.add(AlertLevel.LIST);

        if (activeLevels.isEmpty())
            return empty(pe);

        // Find the highest enabled level for each category
        AlertLevel bestGlow = AlertLevel.NONE;
        AlertLevel bestChat = AlertLevel.NONE;
        AlertLevel bestSound = AlertLevel.NONE;

        for (AlertLevel level : activeLevels) {
            if (!level.isEnabled(config))
                continue;

            if (level.weight > bestGlow.weight && level.shouldGlow(config))
                bestGlow = level;

            // Chat and sound are NONE if muted
            if (!muted) {
                if (level.weight > bestChat.weight && level.shouldChat(config))
                    bestChat = level;
                if (level.weight > bestSound.weight && level.shouldSound(config))
                    bestSound = level;
            }
        }

        return new AlertResponse(pe, bestGlow, bestChat, bestSound);
    }
}