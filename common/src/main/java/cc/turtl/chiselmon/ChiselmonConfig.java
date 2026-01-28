package cc.turtl.chiselmon;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = ChiselmonConstants.MOD_ID)
public class ChiselmonConfig implements ConfigData {

    @ConfigEntry.Gui.Tooltip
    public boolean modDisabled = false;

    // custom validation to run on save and load
    @Override
    public void validatePostLoad() {
    }
}