package cc.turtl.chiselmon.config;

import cc.turtl.chiselmon.feature.pc.sort.PokemonCustomSortMode;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class PcConfig implements ConfigData {

    @ConfigEntry.Gui.Tooltip
    public boolean quickSortEnabled = false;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public PokemonCustomSortMode quickSortMode = PokemonCustomSortMode.POKEDEX_NUMBER;

    @ConfigEntry.Gui.Tooltip
    public boolean bookmarksEnabled = true;

    @ConfigEntry.Gui.Tooltip
    public boolean showEggPreview = false;

    @ConfigEntry.Gui.CollapsibleObject(startExpanded = false)
    public PcTooltipConfig tooltip = new PcTooltipConfig();

    @ConfigEntry.Gui.CollapsibleObject(startExpanded = false)
    public PcIconConfig icons = new PcIconConfig();

    public static class PcTooltipConfig implements ConfigData {
        @ConfigEntry.Gui.Tooltip
        public boolean showTooltips = false;

        @ConfigEntry.Gui.Tooltip
        public boolean showDetailedTooltipOnShift = false;
    }

    public static class PcIconConfig implements ConfigData {
        @ConfigEntry.Gui.Tooltip
        public boolean hiddenAbility = true;

        @ConfigEntry.Gui.Tooltip
        public boolean highIvs = true;

        @ConfigEntry.Gui.Tooltip
        public boolean shiny = true;

        @ConfigEntry.Gui.Tooltip
        public boolean extremeSize = true;

        @ConfigEntry.Gui.Tooltip
        public boolean rideable = false;
    }
}