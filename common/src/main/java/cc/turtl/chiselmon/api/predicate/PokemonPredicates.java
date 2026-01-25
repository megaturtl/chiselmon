package cc.turtl.chiselmon.api.predicate;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.properties.HiddenAbilityProperty;

import cc.turtl.chiselmon.Chiselmon;
import cc.turtl.chiselmon.ChiselmonConfig;
import cc.turtl.chiselmon.api.data.SimpleSpecies;
import cc.turtl.chiselmon.api.data.SimpleSpeciesRegistry;
import cc.turtl.chiselmon.api.util.PokemonCalcUtil;
import cc.turtl.chiselmon.feature.eggpreview.NeoDaycareEggDummy;

public final class PokemonPredicates {
    private PokemonPredicates() {
    }

    public static final Predicate<Pokemon> IS_SHINY = Pokemon::getShiny;

    private static Predicate<Pokemon> hasLabel(String label) {
        return pokemon -> {
            SimpleSpecies species = SimpleSpeciesRegistry.getByName(pokemon.getSpecies().getName());
            return species != null && species.labels.contains(label);
        };
    }

    public static final Predicate<Pokemon> IS_LEGENDARY = hasLabel("legendary");
    public static final Predicate<Pokemon> IS_MYTHICAL = hasLabel("mythical");
    public static final Predicate<Pokemon> IS_ULTRABEAST = hasLabel("ultra_beast");
    public static final Predicate<Pokemon> IS_PARADOX = hasLabel("paradox");

    public static final Predicate<Pokemon> IS_SPECIAL = IS_LEGENDARY.or(IS_MYTHICAL).or(IS_ULTRABEAST);

    public static final Predicate<Pokemon> HAS_HIDDEN_ABILITY = pokemon -> {
        if (PokemonCalcUtil.countUniqueAbilities(pokemon) <= 1) {
            return false;
        }
        return new HiddenAbilityProperty(true).matches(pokemon);
    };

    public static final Predicate<Pokemon> HAS_HIGH_IVS = pokemon -> {
        ChiselmonConfig config = Chiselmon.services().config().get();
        return PokemonCalcUtil.countPerfectIVs(pokemon) >= config.threshold.maxIvs;
    };

    public static final Predicate<Pokemon> IS_EXTREME_SMALL = pokemon -> {
        ChiselmonConfig config = Chiselmon.services().config().get();
        return config != null && pokemon.getScaleModifier() <= config.threshold.extremeSmall;
    };

    public static final Predicate<Pokemon> IS_EXTREME_LARGE = pokemon -> {
        ChiselmonConfig config = Chiselmon.services().config().get();
        return config != null && pokemon.getScaleModifier() >= config.threshold.extremeLarge;
    };

    public static final Predicate<Pokemon> IS_EXTREME_SIZE = IS_EXTREME_SMALL.or(IS_EXTREME_LARGE);

    public static final Predicate<Pokemon> IS_RIDEABLE = pokemon -> !(pokemon.getRiding().getBehaviours() == null);

    public static final Predicate<Pokemon> HAS_SELF_DAMAGING_MOVE = pokemon -> {
        Set<MoveTemplate> possibleMoves = PokemonCalcUtil.getPossibleMoves(pokemon, true);
        List<MoveTemplate> possibleSelfDamagingMoves = possibleMoves.stream()
                .filter(MovePredicates.IS_SELF_DAMAGING)
                .toList();

        return !possibleSelfDamagingMoves.isEmpty();
    };

    public static final Predicate<Pokemon> IS_MARKED = pokemon -> !pokemon.getMarks().isEmpty();

    public static Predicate<Pokemon> isInCustomList(List<String> customList) {
        return pokemon -> {
            if (customList == null || customList.isEmpty()) {
                return false;
            }
            String speciesName = pokemon.getSpecies().getName();
            return customList.stream()
                    .anyMatch(name -> name.equalsIgnoreCase(speciesName));
        };
    }

    public static final Predicate<Pokemon> IS_NEODAYCARE_EGG = pokemon -> NeoDaycareEggDummy.isEgg(pokemon);
    public static final Predicate<Pokemon> IS_NEODAYCARE_DUMMY = pokemon -> NeoDaycareEggDummy.isDummy(pokemon);
}
