package cc.turtl.chiselmon.api.predicate;

import cc.turtl.chiselmon.api.calc.PokemonCalcs;
import cc.turtl.chiselmon.api.species.ClientSpecies;
import cc.turtl.chiselmon.api.species.ClientSpeciesRegistry;
import cc.turtl.chiselmon.config.ChiselmonConfig;
import cc.turtl.chiselmon.feature.eggspy.EggDummy;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.properties.HiddenAbilityProperty;

import java.util.List;
import java.util.function.Predicate;

public final class PokemonPredicates {

    // Simple predicates (no config needed)
    public static final Predicate<Pokemon> IS_SHINY = Pokemon::getShiny;
    public static final Predicate<Pokemon> IS_RIDEABLE = p -> p.getRiding().getBehaviours() != null;
    public static final Predicate<Pokemon> IS_MARKED = p -> !p.getMarks().isEmpty();
    public static final Predicate<Pokemon> IS_LEGENDARY = hasAnyLabel(List.of("legendary", "mythical", "ultra_beast"));
    public static final Predicate<Pokemon> IS_ULTRABEAST = hasAnyLabel(List.of("ultra_beast"));
    public static final Predicate<Pokemon> IS_EGG = p -> p.getSpecies().getResourceIdentifier().equals(EggDummy.EGG_SPECIES_ID);
    public static final Predicate<Pokemon> IS_EGG_DUMMY = p -> p.getForcedAspects().contains(EggDummy.DUMMY_ASPECT);

    // Cobblemon treats Pokemon with a single ability as having HA so we need to check this first
    public static final Predicate<Pokemon> HAS_HIDDEN_ABILITY = p ->
            PokemonCalcs.countUniqueAbilities(p) > 1 && new HiddenAbilityProperty(true).matches(p);

    public static final Predicate<Pokemon> HAS_SELF_DAMAGING_MOVE = p ->
            PokemonCalcs.getPossibleMoves(p, true).stream().anyMatch(MoveTemplatePredicates.IS_SELF_DAMAGING);

    // Config-dependent predicates (lazily fetch config when evaluated)
    public static final Predicate<Pokemon> HAS_HIGH_IVS = p ->
            PokemonCalcs.countPerfectIVs(p) >= ChiselmonConfig.get().general.thresholds.maxIvs;

    public static final Predicate<Pokemon> IS_EXTREME_SMALL = p ->
            p.getScaleModifier() <= ChiselmonConfig.get().general.thresholds.extremeSmall;

    public static final Predicate<Pokemon> IS_EXTREME_LARGE = p ->
            p.getScaleModifier() >= ChiselmonConfig.get().general.thresholds.extremeLarge;

    public static final Predicate<Pokemon> IS_EXTREME_SIZE = IS_EXTREME_SMALL.or(IS_EXTREME_LARGE);

    private PokemonPredicates() {
    }

    private static Predicate<Pokemon> hasAnyLabel(List<String> labels) {
        return p -> {
            ClientSpecies species = ClientSpeciesRegistry.get(p.getSpecies().getName());
            if (species == null) return false;
            for (String label : labels) {
                if (species.labels().contains(label)) return true;
            }
            return false;
        };
    }
}