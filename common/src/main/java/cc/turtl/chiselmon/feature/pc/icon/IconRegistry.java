package cc.turtl.chiselmon.feature.pc.icon;

import cc.turtl.chiselmon.api.predicate.PokemonPredicates;
import cc.turtl.chiselmon.api.OLDPCConfig;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static cc.turtl.chiselmon.util.MiscUtil.modResource;

public class IconRegistry {
    private static final List<IconEntry> ENTRIES = new ArrayList<>();

    static {
        add("hidden_ability", cfg -> cfg.hidden_ability, PokemonPredicates.HAS_HIDDEN_ABILITY);
        add("ivs", cfg -> cfg.ivs, PokemonPredicates.HAS_HIGH_IVS);
        add("shiny", cfg -> cfg.shiny, PokemonPredicates.IS_SHINY);
        add("size", cfg -> cfg.size, PokemonPredicates.IS_EXTREME_SIZE);
        add("mark", cfg -> cfg.mark, PokemonPredicates.IS_MARKED);
        add("rideable", cfg -> cfg.rideable, PokemonPredicates.IS_RIDEABLE);
    }

    private static void add(String path, Predicate<OLDPCConfig.PcIconConfig> cfg, Predicate<Pokemon> pkmn) {
        ResourceLocation resource = modResource("textures/gui/pc/icon/icon_" + path + ".png");
        ENTRIES.add(new IconEntry(resource, cfg, pkmn));
    }

    public static List<IconEntry> getEntries() { return ENTRIES; }
}