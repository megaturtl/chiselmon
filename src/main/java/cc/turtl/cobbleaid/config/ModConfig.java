package cc.turtl.cobbleaid.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "cobbleaid")
public class ModConfig implements ConfigData {

    @ConfigEntry.Gui.Tooltip public boolean modDisabled = false;
    @ConfigEntry.Gui.Tooltip public boolean debugMode = false;


    @ConfigEntry.Gui.CollapsibleObject
    public PcConfig pcConfig = new PcConfig();

    @ConfigEntry.Gui.Tooltip
    public float extremeSmallThreshold = 0.5F;

    @ConfigEntry.Gui.Tooltip
    public float extremeLargeThreshold = 1.5F;

    @ConfigEntry.Gui.Tooltip
    public int highIVTotalThreshold = 180;

    public static class PcConfig {
        @ConfigEntry.Gui.Tooltip
        public boolean showHiddenAbility = true;

        @ConfigEntry.Gui.Tooltip
        public boolean showHighIvs = true;

        @ConfigEntry.Gui.Tooltip
        public boolean showShiny = false;
        
        @ConfigEntry.Gui.Tooltip
        public boolean showExtremeSize = false;
    }

    // custom validation to run on save and load
    public ModConfig validate_fields() {
        // put logic here
        return this;
    }
}
