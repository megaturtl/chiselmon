package cc.turtl.chiselmon.feature.pc.tooltip;

import cc.turtl.chiselmon.api.predicate.PokemonPredicates;
import cc.turtl.chiselmon.api.OLDPCConfig;
import cc.turtl.chiselmon.util.format.PokemonFormats;
import com.cobblemon.mod.common.pokemon.Pokemon;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class TooltipRegistry {
    private static final List<TooltipEntry> ENTRIES = new ArrayList<>();

    static {
        // Register standard entries
        add("ivs", cfg -> cfg.showIvs, p -> true, PokemonFormats::ivsSummary);
        add("original_trainer", cfg -> cfg.showOriginalTrainer, p -> true, Pokemon::getOriginalTrainerName);
        add("form", cfg -> cfg.showForm, p -> true, p -> p.getForm().getName());
        add("friendship", cfg -> cfg.showFriendship, p -> true, Pokemon::getFriendship);

        // Register conditional entries
        add("ride_styles", cfg -> cfg.showRideStyles, PokemonPredicates.IS_RIDEABLE, PokemonFormats::rideStyles);
        add("marks", cfg -> cfg.showMarks, PokemonPredicates.IS_MARKED, PokemonFormats::marks);
        add("hatch_progress", cfg -> cfg.showHatchProgress, PokemonPredicates.IS_EGG_DUMMY, PokemonFormats::hatchProgress);
    }

    private static void add(String key, Predicate<OLDPCConfig.PCTooltipConfig> configCheck, Predicate<Pokemon> pokemonCheck, Function<Pokemon, Object> val) {
        ENTRIES.add(new TooltipEntry(key, configCheck, pokemonCheck, val));
    }

    public static List<TooltipEntry> getEntries() {
        return ENTRIES;
    }
}