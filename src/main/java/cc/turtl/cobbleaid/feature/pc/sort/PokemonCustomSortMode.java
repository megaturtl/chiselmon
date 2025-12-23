package cc.turtl.cobbleaid.feature.pc.sort;

import cc.turtl.cobbleaid.api.comparator.PokemonComparators;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.resources.ResourceLocation;

import java.util.Comparator;

import static cc.turtl.cobbleaid.util.TextUtil.modResource;

public enum PokemonCustomSortMode {

    SIZE(
        "size",
        PokemonComparators.SIZE_COMPARATOR,
        true
    ),

    IVS(
        "ivs",
        PokemonComparators.IVS_COMPARATOR,
        true
    ),
    LEVEL(
        "ivs",
        PokemonComparators.LEVEL_COMPARATOR,
        true
    ),
    POKEDEX_NUMBER(
        "ivs",
        PokemonComparators.POKEDEX_COMPARATOR,
        true
    );

    private final String id;
    private final Comparator<Pokemon> comparator;
    private final boolean showInUI;

    private final String tooltipKey;
    private final String labelKey;
    private final ResourceLocation icon;
    private final ResourceLocation iconReversed;

    PokemonCustomSortMode(
            String id,
            Comparator<Pokemon> comparator,
            boolean showInUI
    ) {
        this.id = id;
        this.comparator = comparator;
        this.showInUI = showInUI;

        this.tooltipKey = "ui.sort." + id;
        this.labelKey = "sort_" + id;
        this.icon = modResource("textures/gui/pc/pc_button_sort_" + id + ".png");
        this.iconReversed = modResource("textures/gui/pc/pc_button_sort_" + id + "_reverse.png");
    }

    public Comparator<Pokemon> comparator(boolean reversed) {
        return reversed ? comparator.reversed() : comparator;
    }

    /* ---- UI metadata ---- */

    public String getId() {
        return id;
    }

    public boolean showInUI() {
        return showInUI;
    }

    public String tooltipKey() {
        return tooltipKey;
    }

    public String labelKey() {
        return labelKey;
    }

    public ResourceLocation icon() {
        return icon;
    }

    public ResourceLocation iconReversed() {
        return iconReversed;
    }
}
