package cc.turtl.cobbleaid.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "cobbleaid")
public class ModConfig implements ConfigData {

    @ConfigEntry.Gui.Tooltip public boolean modDisabled = false;

    @ConfigEntry.Gui.Tooltip public boolean debugMode = false;

    // custom validation to run on save and load
    public ModConfig validate_fields() {
        // put logic here

        return this;
    }
}
