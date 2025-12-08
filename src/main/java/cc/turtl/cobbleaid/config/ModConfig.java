package cc.turtl.cobbleaid.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "cobbleaid")
public class ModConfig implements ConfigData {

    @ConfigEntry.Gui.Tooltip public boolean modDisabled = false;
    @ConfigEntry.Gui.Tooltip public boolean debugMode = false;
    
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Category("pc")
    public boolean showHiddenAbilityIcons = true;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Category("pc")
    public boolean showMaxIvsIcons = true;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Category("pc")
    public boolean showShinyIcons = true;
    
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Category("pc")
    public boolean showExtremeSizeIcons = false;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Category("pc")
    public boolean showRideableIcons = false;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Category("pc")
    public boolean showEggPreview = false;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Category("pc")
    public boolean showTooltips = false;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Category("pc")
    public boolean showDetailedTooltipOnShift = false;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Category("misc")
    public boolean showPokeRodBaitAboveHotbar = true;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Category("threshold")
    public float extremeSmallThreshold = 0.5F;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Category("threshold")
    public float extremeLargeThreshold = 1.5F;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Category("threshold")
    public int maxIvsThreshold = 5;

    // custom validation to run on save and load
    public ModConfig validate_fields() {
        // put logic here
        return this;
    }
}
