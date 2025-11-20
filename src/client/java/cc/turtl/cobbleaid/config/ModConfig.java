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

    // custom validation to run on save and load
    public ModConfig validate_fields() {
        // put logic here

        return this;
    }

    public static class PcHighlighting {
        @ConfigEntry.Gui.Tooltip
        public boolean highlightHiddenAbility = true;

        @ConfigEntry.ColorPicker(allowAlpha = true)
        public int hiddenAbilityColor = 0x804E6CE7;

        @ConfigEntry.Gui.Tooltip
        public boolean highlightMaxIV = true;

        @ConfigEntry.ColorPicker(allowAlpha = true)
        public int maxIvColor = 0x80F6C667;

        @ConfigEntry.Gui.Tooltip
        public boolean highlightShiny = true;

        @ConfigEntry.ColorPicker(allowAlpha = true)
        public int shinyColor = 0x80FFB347;
    }
}
