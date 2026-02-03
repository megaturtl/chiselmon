package cc.turtl.chiselmon.feature.pc.sort;

import cc.turtl.chiselmon.api.comparator.PokemonComparators;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.resources.ResourceLocation;

import java.util.Comparator;

import static cc.turtl.chiselmon.util.MiscUtil.modResource;

public enum SortMode {

    SIZE(
            "size",
            "Size",
            PokemonComparators.SIZE_COMPARATOR,
            true),
    IVS(
            "ivs",
            "IVs",
            PokemonComparators.IVS_COMPARATOR,
            true),
    LEVEL(
            "level",
            "Level",
            PokemonComparators.LEVEL_COMPARATOR,
            false),
    POKEDEX_NUMBER(
            "pokedex",
            "Pokédex Number",
            PokemonComparators.POKEDEX_COMPARATOR,
            false);

    private final String id;
    private final String displayName;
    private final Comparator<Pokemon> comparator;
    private final boolean showInUI;

    private final String tooltipKey;
    private final String labelKey;
    private final ResourceLocation icon;
    private final ResourceLocation iconReversed;

    SortMode(
            String id,
            String displayName,
            Comparator<Pokemon> comparator,
            boolean showInUI) {
        this.id = id;
        this.displayName = displayName;
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

    @Override
    public String toString() {
        return displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
