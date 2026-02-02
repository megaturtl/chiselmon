package cc.turtl.chiselmon.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class PCConfig implements ConfigData {

    @ConfigEntry.Gui.Tooltip
    public boolean bookmarksEnabled = true;

    @ConfigEntry.Gui.CollapsibleObject(startExpanded = false)
    public PCTooltipConfig tooltip = new PCTooltipConfig();

    @ConfigEntry.Gui.CollapsibleObject(startExpanded = false)
    public PcIconConfig icons = new PcIconConfig();

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
        public boolean hiddenAbility = true;
        @ConfigEntry.Gui.Tooltip
        public boolean highIvs = true;
        @ConfigEntry.Gui.Tooltip
        public boolean shiny = true;
        @ConfigEntry.Gui.Tooltip
        public boolean extremeSize = true;
        @ConfigEntry.Gui.Tooltip
        public boolean marked = true;
        @ConfigEntry.Gui.Tooltip
        public boolean rideable = false;
    }
}