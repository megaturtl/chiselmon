package cc.turtl.chiselmon.system.group;

import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.api.Priority;
import cc.turtl.chiselmon.util.format.ColorUtils;

public final class DefaultPokemonGroups {

    public static final PokemonGroup LEGENDARIES = new PokemonGroupBuilder()
            .id("legendaries")
            .name("Legendary Pokemon")
            .rgb(ColorUtils.MAGENTA)
            .priority(Priority.HIGHEST)
            .legendary()
            .build();

    public static final PokemonGroup SHINIES = new PokemonGroupBuilder()
            .id("shinies")
            .name("Shiny Pokemon")
            .rgb(ColorUtils.GOLD)
            .priority(Priority.HIGH)
            .shiny()
            .build();

    public static final PokemonGroup EXTREME_SIZES = new PokemonGroupBuilder()
            .id("extreme_sizes")
            .name("Extreme Size Pokemon")
            .rgb(ColorUtils.TEAL)
            .priority(Priority.NORMAL)
            .sizeRange(ChiselmonConstants.CONFIG.threshold.extremeSmall, ChiselmonConstants.CONFIG.threshold.extremeLarge)
            .build();

    private DefaultPokemonGroups() {
    }
}