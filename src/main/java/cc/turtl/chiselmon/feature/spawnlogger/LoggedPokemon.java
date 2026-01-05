package cc.turtl.chiselmon.feature.spawnlogger;

public record LoggedPokemon(
        String species,
        int level,
        boolean isShiny,
        boolean isSpecial, // leg/mythical/ultrabeast
        boolean isExtremeSize,
        float scaleModifier,
        int x,
        int y,
        int z,
        String dimension,
        String biome,
        long timeLoggedMs) {
}