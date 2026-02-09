package cc.turtl.chiselmon.system.group;

import cc.turtl.chiselmon.api.Priority;
import cc.turtl.chiselmon.api.predicate.PokemonPredicates;
import cc.turtl.chiselmon.util.format.ColorUtils;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.Pokemon;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Builder for creating custom Pokemon groups.
 */
public class PokemonGroupBuilder {
    private String id;
    private String name;
    private int rgb = ColorUtils.WHITE;
    private Priority priority = Priority.NORMAL;
    private final List<Predicate<Pokemon>> conditions = new ArrayList<>();

    public PokemonGroupBuilder id(String id) {
        this.id = id;
        return this;
    }

    public PokemonGroupBuilder name(String name) {
        this.name = name;
        return this;
    }

    public PokemonGroupBuilder rgb(int rgb) {
        this.rgb = rgb;
        return this;
    }

    public PokemonGroupBuilder priority(Priority priority) {
        this.priority = priority;
        return this;
    }

    public PokemonGroupBuilder addCondition(Predicate<Pokemon> condition) {
        conditions.add(condition);
        return this;
    }

    public PokemonGroupBuilder shiny() {
        return addCondition(PokemonPredicates.IS_SHINY);
    }

    public PokemonGroupBuilder legendary() {
        return addCondition(PokemonPredicates.IS_LEGENDARY);
    }

    public PokemonGroupBuilder sizeRange(float min, float max) {
        return addCondition(pokemon -> {
            float scale = pokemon.getScaleModifier();
            return scale >= min && scale <= max;
        });
    }

    public PokemonGroupBuilder species(String... speciesNames) {
        Set<String> targetSpecies = new HashSet<>();
        for (String s : speciesNames) targetSpecies.add(s.toLowerCase());

        return addCondition(pokemon ->
                targetSpecies.contains(pokemon.getSpecies().getName().toLowerCase())
        );
    }

    public PokemonGroupBuilder type(String typeName) {
        return addCondition(pokemon -> {
            for (var type : pokemon.getTypes()) {
                if (type.getName().equalsIgnoreCase(typeName)) {
                    return true;
                }
            }
            return false;
        });
    }

    public PokemonGroupBuilder gender(Gender gender) {
        return addCondition(pokemon -> pokemon.getGender().equals(gender));
    }

    public PokemonGroup build() {
        if (id == null || id.isBlank()) {
            throw new IllegalStateException("Group ID is required");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalStateException("Group name is required");
        }
        if (conditions.isEmpty()) {
            throw new IllegalStateException("At least one condition is required");
        }

        // Combine all conditions with AND logic
        Predicate<Pokemon> combined = conditions.stream()
                .reduce(Predicate::and)
                .orElse(p -> true);

        return new PokemonGroup(id, name, rgb, priority, combined);
    }
}