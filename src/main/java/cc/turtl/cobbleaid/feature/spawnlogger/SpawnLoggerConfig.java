package cc.turtl.cobbleaid.feature.spawnlogger;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class SpawnLoggerConfig implements ConfigData {

    @ConfigEntry.Gui.Tooltip
    public boolean enabled = false;

    @Override
    public void validatePostLoad() throws ValidationException {
    }
}