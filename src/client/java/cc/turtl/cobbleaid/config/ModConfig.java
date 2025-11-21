package cc.turtl.cobbleaid.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "cobbleaid")
public class ModConfig implements ConfigData {

    @ConfigEntry.Gui.Tooltip public boolean modDisabled = false;

    @ConfigEntry.Gui.Tooltip public boolean debugMode = false;

    @ConfigEntry.Gui.CollapsibleObject
    public PcHighlighting pcHighlighting = new PcHighlighting();

    @ConfigEntry.Gui.Tooltip
    public float extremeSmallThreshold = 0.25F;

    @ConfigEntry.Gui.Tooltip
    public float extremeLargeThreshold = 1.75F;

    @ConfigEntry.Gui.Tooltip
    public int highIVTotalThreshold = 180;

    // custom validation to run on save and load
    public ModConfig validate_fields() {
        // put logic here

        return this;
    }

    public static class PcHighlighting {
        @ConfigEntry.Gui.Tooltip
        public boolean highlightHiddenAbility = true;

        @ConfigEntry.ColorPicker(allowAlpha = false)
        public int hiddenAbilityColor = 0x4E6CE7;

        @ConfigEntry.Gui.Tooltip
        public boolean highlightHighIV = true;

        @ConfigEntry.ColorPicker(allowAlpha = false)
        public int highIVColor = 0x4CAF50;

        @ConfigEntry.Gui.Tooltip
        public boolean highlightShiny = true;

        @ConfigEntry.ColorPicker(allowAlpha = false)
        public int shinyColor = 0xFFB347;

        @ConfigEntry.Gui.Tooltip
        public boolean highlightExtremeSmall = false;

        @ConfigEntry.ColorPicker(allowAlpha = false)
        public int extremeSmallColor = 0x7FC7FF;

        @ConfigEntry.Gui.Tooltip
        public boolean highlightExtremeLarge = false;

        @ConfigEntry.ColorPicker(allowAlpha = false)
        public int extremeLargeColor = 0xE071FF;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 0, max = 255)
        public int highlightAlpha = 128;
    }
}
