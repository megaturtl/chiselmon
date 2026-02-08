package cc.turtl.chiselmon.api.predicate;

import cc.turtl.chiselmon.Chiselmon;
import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.api.calc.PokemonCalcs;
import cc.turtl.chiselmon.api.data.species.ClientSpecies;
import cc.turtl.chiselmon.api.data.species.ClientSpeciesRegistry;
import cc.turtl.chiselmon.feature.pc.eggpreview.EggDummy;
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
    public static final Predicate<Pokemon> HAS_HIGH_IVS = p ->
            PokemonCalcs.countPerfectIVs(p) >= ChiselmonConstants.CONFIG.threshold.maxIvs;
    public static final Predicate<Pokemon> IS_EXTREME_SMALL = p ->
                p.getScaleModifier() <= ChiselmonConstants.CONFIG.threshold.extremeSmall;
    public static final Predicate<Pokemon> IS_EXTREME_LARGE = p ->
            p.getScaleModifier() <= ChiselmonConstants.CONFIG.threshold.extremeSmall;
    public static final Predicate<Pokemon> IS_EXTREME_SIZE = IS_EXTREME_SMALL.or(IS_EXTREME_LARGE);
    public static final Predicate<Pokemon> IS_EGG = p -> p.getSpecies().getResourceIdentifier() == EggDummy.EGG_SPECIES_ID;
    public static final Predicate<Pokemon> IS_EGG_DUMMY = p -> p.getForcedAspects().contains(EggDummy.DUMMY_ASPECT);

    private PokemonPredicates() {
    }

    private static Predicate<Pokemon> hasLabel(String label) {
        return p -> {
            ClientSpecies species = ClientSpeciesRegistry.get(p.getSpecies().getName());
            return species != null && species.labels().contains(label);
        };
    }
}
