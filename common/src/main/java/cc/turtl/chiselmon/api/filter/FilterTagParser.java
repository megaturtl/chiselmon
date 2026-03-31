package cc.turtl.chiselmon.api.filter;

import cc.turtl.chiselmon.api.predicate.PokemonPredicates;
import cc.turtl.chiselmon.util.ParseUtils;
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
            return p -> false;
        }

        String[] parts = tag.split("=", 2);
        String key = parts[0].toLowerCase().trim();
        String rawValue = parts[1].trim();

        return switch (key) {
            case "species" -> {
                String normalizedSearch = ParseUtils.normalizeSpeciesName(rawValue);
                yield pokemon -> {
                    String internalName = ParseUtils.normalizeSpeciesName(pokemon.getSpecies().getName());
                    return internalName.equals(normalizedSearch);
                };
            }

            case "type" -> pokemon -> StreamSupport.stream(pokemon.getTypes().spliterator(), false)
                    .anyMatch(t -> t.getName().equalsIgnoreCase(rawValue));

            case "gender" -> pokemon -> {
                try {
                    return pokemon.getGender().equals(Gender.valueOf(rawValue.toUpperCase()));
                } catch (Exception e) {
                    return false;
                }
            };

            case "min_size" -> pokemon -> {
                try {
                    return pokemon.getScaleModifier() >= Float.parseFloat(rawValue);
                } catch (Exception e) {
                    return false;
                }
            };

            case "max_size" -> pokemon -> {
                try {
                    return pokemon.getScaleModifier() <= Float.parseFloat(rawValue);
                } catch (Exception e) {
                    return false;
                }
            };

            case "min_level" -> pokemon -> {
                try { return pokemon.getLevel() >= Integer.parseInt(rawValue); }
                catch (Exception e) { return false; }
            };

            default -> p -> false;
        };
    }
}