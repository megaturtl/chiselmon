package cc.turtl.cobbleaid.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class ThresholdConfig implements ConfigData {
    @ConfigEntry.Gui.Tooltip
    public float extremeSmall = 0.5F;

    @ConfigEntry.Gui.Tooltip
    public float extremeLarge = 1.5F;

    @ConfigEntry.Gui.Tooltip
    public int maxIvs = 5;
}
