package cc.turtl.chiselmon.api;

import cc.turtl.chiselmon.feature.pc.sort.SortMode;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class OLDPCConfig implements ConfigData {

    @ConfigEntry.Gui.Tooltip
    public boolean bookmarksEnabled = true;

    @ConfigEntry.Gui.Tooltip
    public boolean quickSortEnabled = false;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public SortMode quickSortMode = SortMode.POKEDEX_NUMBER;

    @ConfigEntry.Gui.Tooltip
    public boolean eggPreviewEnabled = true;

    @ConfigEntry.Gui.CollapsibleObject(startExpanded = false)
    public PCTooltipConfig tooltip = new PCTooltipConfig();

    @ConfigEntry.Gui.CollapsibleObject(startExpanded = false)
    public PcIconConfig icon = new PcIconConfig();

    public static class PCTooltipConfig implements ConfigData {
        @ConfigEntry.Gui.Tooltip
        public boolean showOnHover = false;
        @ConfigEntry.Gui.Tooltip
        public boolean extendOnShift = true;
        @ConfigEntry.Gui.Tooltip
        public boolean showIvs = true;
        public boolean showOriginalTrainer = true;
        public boolean showForm = true;
        public boolean showFriendship = false;
        @ConfigEntry.Gui.Tooltip
        public boolean showRideStyles = true;
        @ConfigEntry.Gui.Tooltip
        public boolean showMarks = true;
        @ConfigEntry.Gui.Tooltip
        public boolean showHatchProgress = false;
    }

    public static class PcIconConfig implements ConfigData {
        @ConfigEntry.Gui.Tooltip
        public boolean hidden_ability = true;
        @ConfigEntry.Gui.Tooltip
        public boolean ivs = true;
        @ConfigEntry.Gui.Tooltip
        public boolean shiny = true;
        @ConfigEntry.Gui.Tooltip
        public boolean size = true;
        @ConfigEntry.Gui.Tooltip
        public boolean mark = true;
        @ConfigEntry.Gui.Tooltip
        public boolean rideable = false;
    }
}