package cc.turtl.chiselmon.feature.pc.tooltip;

import cc.turtl.chiselmon.client.config.category.PCConfig;
import cc.turtl.chiselmon.core.api.predicate.PokemonPredicatesKt;
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
        add("ivs", PCConfig.TooltipConfig::getIvs, p -> true, PokemonFormats::ivsSummary);
        add("original_trainer", PCConfig.TooltipConfig::getOriginalTrainer, p -> true, Pokemon::getOriginalTrainerName);
        add("form", PCConfig.TooltipConfig::getForm, p -> true, p -> p.getForm().getName());
        add("friendship", PCConfig.TooltipConfig::getFriendship, p -> true, Pokemon::getFriendship);

        // Register conditional entries
        add("ride_styles", PCConfig.TooltipConfig::getRideStyles, PokemonPredicatesKt.IS_RIDEABLE, PokemonFormats::rideStyles);
        add("marks", PCConfig.TooltipConfig::getMarks, PokemonPredicatesKt.IS_MARKED, PokemonFormats::marks);
        add("hatch_progress", PCConfig.TooltipConfig::getHatchProgress, PokemonPredicatesKt.IS_EGG_DUMMY, PokemonFormats::hatchProgress);
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