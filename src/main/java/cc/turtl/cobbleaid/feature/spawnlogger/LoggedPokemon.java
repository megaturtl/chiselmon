package cc.turtl.cobbleaid.feature.spawnlogger;

public record LoggedPokemon(
        String species,
        int level,
        boolean isShiny,
        boolean isSpecial, // leg/mythical/ultrabeast
        boolean isExtremeSize,
        float scaleModifier,
        long timeLoggedMs) {
}