package cc.turtl.cobbleaid.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "cobbleaid")
public class ModConfig implements ConfigData {

    @ConfigEntry.Gui.Tooltip public boolean modDisabled = false;
    @ConfigEntry.Gui.Tooltip public boolean debugMode = false;

    @ConfigEntry.Gui.Tooltip
    public float extremeSmallThreshold = 0.5F;

    @ConfigEntry.Gui.Tooltip
    public float extremeLargeThreshold = 1.5F;

    @ConfigEntry.Gui.Tooltip
    public int maxIvsThreshold = 4;

    @ConfigEntry.Gui.CollapsibleObject
    public PcConfig pcConfig = new PcConfig();

    public static class PcConfig {
        @ConfigEntry.Gui.Tooltip
        public boolean showHiddenAbilityIcons = true;

        @ConfigEntry.Gui.Tooltip
        public boolean showMaxIvsIcons = true;

        @ConfigEntry.Gui.Tooltip
        public boolean showShinyIcons = false;
        
        @ConfigEntry.Gui.Tooltip
        public boolean showExtremeSizeIcons = false;

        @ConfigEntry.Gui.Tooltip
        public boolean showEggPreview = false;

        @ConfigEntry.Gui.Tooltip
        public boolean showTooltips = false;
    }

    // custom validation to run on save and load
    public ModConfig validate_fields() {
        // put logic here
        return this;
    }
}
