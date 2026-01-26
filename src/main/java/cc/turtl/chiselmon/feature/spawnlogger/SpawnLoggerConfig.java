package cc.turtl.chiselmon.feature.spawnlogger;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class SpawnLoggerConfig implements ConfigData {

    @ConfigEntry.Gui.Tooltip
    public boolean enabled = true;

    @ConfigEntry.Gui.Tooltip
    public boolean showActionBarStatus = true;

    @ConfigEntry.Gui.Tooltip
    public boolean autoSaveCsv = false;

    @Override
    public void validatePostLoad() {
    }
}