package cc.turtl.chiselmon.api.filter;

import cc.turtl.chiselmon.api.predicate.PokemonPredicates;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.Pokemon;

import java.util.function.Predicate;
import java.util.stream.StreamSupport;

public class FilterTagParser {

    /**
     * Parses a simple tag string into a Pokemon predicate.
     * <p>
     * Examples:
     * - "shiny" -> IS_SHINY
     * - "legendary" -> IS_LEGENDARY
     * - "type=fire" -> has fire type
     * - "species=pikachu" -> is Pikachu
     * - "min_size=1.5" -> sizes inclusive above 1.5
     * - "max_size=0.4" -> sizes inclusive below 0.4
     * - "gender=male" -> is male
     */
    public static Predicate<Pokemon> parse(String tag) {
        tag = tag.toLowerCase().trim();

        return switch (tag) {
            case "shiny" -> PokemonPredicates.IS_SHINY;
            case "legendary" -> PokemonPredicates.IS_LEGENDARY;
            case "extreme_size" -> PokemonPredicates.IS_EXTREME_SIZE;
            default -> parseComplexTag(tag);
        };
    }

    private static Predicate<Pokemon> parseComplexTag(String tag) {
        if (!tag.contains("=")) {
            return p -> false; // Invalid tag
        }

        String[] parts = tag.split("=", 2);
        String key = parts[0];
        String value = parts[1];

        return switch (key) {
            case "species" -> pokemon ->
                    pokemon.getSpecies().getName().equalsIgnoreCase(value);

            case "type" -> pokemon -> StreamSupport.stream(pokemon.getTypes().spliterator(), false)
                    .anyMatch(t -> t.getName().equalsIgnoreCase(value));

            case "gender" -> pokemon ->
                    pokemon.getGender().equals(Gender.valueOf(value.toUpperCase()));

            case "min_size" -> pokemon -> pokemon.getScaleModifier() >= Float.parseFloat(value);
            case "max_size" -> pokemon -> pokemon.getScaleModifier() <= Float.parseFloat(value);

            default -> p -> false;
        };
    }
}