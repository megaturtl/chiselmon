package cc.turtl.chiselmon.feature.spawnlogger;

public record LoggedPokemon(
        String species,
        String form,
        int level,
        boolean isShiny,
        boolean isSpecial, // leg/mythical/ultrabeast
        boolean isExtremeSize,
        float scaleModifier,
        boolean snackSpawn,
        int x,
        int y,
        int z,
        String dimension,
        String biome,
        long timeLoggedMs) {
}