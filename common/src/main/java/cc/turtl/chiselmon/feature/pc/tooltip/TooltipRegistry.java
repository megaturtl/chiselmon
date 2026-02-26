package cc.turtl.chiselmon.feature.pc.tooltip;

import cc.turtl.chiselmon.api.predicate.PokemonPredicates;
import cc.turtl.chiselmon.config.category.PCConfig;
import cc.turtl.chiselmon.util.format.PokemonFormats;
import com.cobblemon.mod.common.pokemon.Pokemon;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public final class TooltipRegistry {
    private static final List<TooltipEntry> ENTRIES = new ArrayList<>();

    static {
        // Register standard entries
        add("ivs", cfg -> cfg.ivs, p -> true, PokemonFormats::ivsSummary);
        add("original_trainer", cfg -> cfg.originalTrainer, p -> true, Pokemon::getOriginalTrainerName);
        add("form", cfg -> cfg.form, p -> true, p -> p.getForm().getName());
        add("friendship", cfg -> cfg.friendship, p -> true, Pokemon::getFriendship);

        // Register conditional entries
        add("ride_styles", cfg -> cfg.rideStyles, PokemonPredicates.IS_RIDEABLE, PokemonFormats::rideStyles);
        add("marks", cfg -> cfg.marks, PokemonPredicates.IS_MARKED, PokemonFormats::marks);
        add("hatch_progress", cfg -> cfg.hatchProgress, PokemonPredicates.IS_EGG_DUMMY, PokemonFormats::hatchProgress);
    }

    private TooltipRegistry() {
    }

    private static void add(String key, Predicate<PCConfig.TooltipConfig> configCheck, Predicate<Pokemon> pokemonCheck, Function<Pokemon, Object> val) {
        ENTRIES.add(new TooltipEntry(key, configCheck, pokemonCheck, val));
    }

    public static List<TooltipEntry> getEntries() {
        return ENTRIES;
    }
}