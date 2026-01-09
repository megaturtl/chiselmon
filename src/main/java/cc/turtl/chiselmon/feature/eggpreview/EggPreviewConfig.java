package cc.turtl.chiselmon.feature.eggpreview;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class EggPreviewConfig implements ConfigData {

    @ConfigEntry.Gui.Tooltip
    public boolean enabled = false;

    @ConfigEntry.Gui.Tooltip
    public boolean attemptHatchSync = false;
}
