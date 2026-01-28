package cc.turtl.chiselmon.api.predicate;

import cc.turtl.chiselmon.api.calc.PokemonCalcs;
import cc.turtl.chiselmon.api.data.species.ClientSpecies;
import cc.turtl.chiselmon.api.data.species.ClientSpeciesRegistry;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.properties.HiddenAbilityProperty;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

public final class PokemonPredicates {
    public static final Predicate<Pokemon> IS_SHINY = Pokemon::getShiny;
    public static final Predicate<Pokemon> IS_RIDEABLE = p -> p.getRiding().getBehaviours() != null;
    public static final Predicate<Pokemon> IS_MARKED = p -> !p.getMarks().isEmpty();
    public static final Predicate<Pokemon> IS_LEGENDARY = hasLabel("legendary");
    public static final Predicate<Pokemon> IS_MYTHICAL = hasLabel("mythical");
    public static final Predicate<Pokemon> IS_ULTRABEAST = hasLabel("ultra_beast");
    public static final Predicate<Pokemon> IS_PARADOX = hasLabel("paradox");
    public static final Predicate<Pokemon> IS_SPECIAL = IS_LEGENDARY.or(IS_MYTHICAL).or(IS_ULTRABEAST);
    // Cobblemon treats Pokemon with a single ability as having HA so we need to check this first
    public static final Predicate<Pokemon> HAS_HIDDEN_ABILITY = p ->
            PokemonCalcs.countUniqueAbilities(p) > 1 && new HiddenAbilityProperty(true).matches(p);
    public static final Predicate<Pokemon> HAS_SELF_DAMAGING_MOVE = p ->
            PokemonCalcs.getPossibleMoves(p, true).stream().anyMatch(MoveTemplatePredicates.IS_SELF_DAMAGING);
    public static final BiPredicate<Pokemon, Integer> HAS_HIGH_IVS = (p, threshold) ->
            PokemonCalcs.countPerfectIVs(p) >= threshold;
    public static final BiPredicate<Pokemon, Float> IS_SMALLER_THAN = (p, scale) ->
            p.getScaleModifier() <= scale;
    public static final BiPredicate<Pokemon, Float> IS_LARGER_THAN = (p, scale) ->
            p.getScaleModifier() >= scale;

    private PokemonPredicates() {
    }

    public static Predicate<Pokemon> isExtremeSize(float smallThreshold, float largeThreshold) {
        return p -> IS_SMALLER_THAN.test(p, smallThreshold) || IS_LARGER_THAN.test(p, largeThreshold);
    }

    private static Predicate<Pokemon> hasLabel(String label) {
        return p -> {
            ClientSpecies species = ClientSpeciesRegistry.get(p.getSpecies().getName());
            return species != null && species.labels().contains(label);
        };
    }
}
