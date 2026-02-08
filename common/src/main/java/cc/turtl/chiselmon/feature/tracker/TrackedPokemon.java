package cc.turtl.chiselmon.feature.tracker;

import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.Species;

public record TrackedPokemon(
        Species species,
        FormData form,
        int level,
        float scaleModifier,
        boolean isShiny,
        boolean isLegendary,
        boolean isExtremeSize,
        boolean snackSpawn,
        int x,
        int y,
        int z,
        String dimension,
        String biome,
        long timeSeenMs) {
}