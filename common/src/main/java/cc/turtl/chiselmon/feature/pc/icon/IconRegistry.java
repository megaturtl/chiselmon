package cc.turtl.chiselmon.feature.pc.icon;

import cc.turtl.chiselmon.client.config.category.PCConfig;
import cc.turtl.chiselmon.core.api.predicate.PokemonPredicatesKt;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static cc.turtl.chiselmon.util.MiscUtil.modResource;

public final class IconRegistry {
    private static final List<IconEntry> ENTRIES = new ArrayList<>();

    static {
        add("hidden_ability", PCConfig.IconConfig::getHiddenAbility, PokemonPredicatesKt.HAS_HIDDEN_ABILITY);
        add("ivs", PCConfig.IconConfig::getIvs, PokemonPredicatesKt.HAS_HIGH_IVS);
        add("shiny", PCConfig.IconConfig::getShiny, PokemonPredicatesKt.IS_SHINY);
        add("size", PCConfig.IconConfig::getSize, PokemonPredicatesKt.IS_EXTREME_SIZE);
        add("mark", PCConfig.IconConfig::getMark, PokemonPredicatesKt.IS_MARKED);
        add("rideable", PCConfig.IconConfig::getRideable, PokemonPredicatesKt.IS_RIDEABLE);
        add("shoulderable", PCConfig.IconConfig::getShoulderable, PokemonPredicatesKt.IS_SHOULDERABLE);
    }

    private IconRegistry() {
    }

    private static void add(String path, Predicate<PCConfig.IconConfig> cfg, Predicate<Pokemon> pkmn) {
        ResourceLocation resource = modResource("textures/gui/pc/icon/icon_" + path + ".png");
        ENTRIES.add(new IconEntry(resource, cfg, pkmn));
    }

    public static List<IconEntry> getEntries() {
        return ENTRIES;
    }
}