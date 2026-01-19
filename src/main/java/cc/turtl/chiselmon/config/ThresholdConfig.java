package cc.turtl.chiselmon.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class ThresholdConfig implements ConfigData {
    @ConfigEntry.Gui.Tooltip
    public float extremeSmall = 0.4F;

    @ConfigEntry.Gui.Tooltip
    public float extremeLarge = 1.6F;

    @ConfigEntry.Gui.Tooltip
    public int maxIvs = 5;
}
