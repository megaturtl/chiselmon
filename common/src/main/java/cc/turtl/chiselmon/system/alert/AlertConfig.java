package cc.turtl.chiselmon.system.alert;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.ArrayList;
import java.util.List;

public class AlertConfig implements ConfigData {

    @ConfigEntry.Gui.Tooltip
    public boolean masterEnabled = true;

    @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
    @ConfigEntry.Gui.Tooltip
    public int masterVolume = 100;

    @ConfigEntry.Gui.Tooltip
    public boolean showFormInMessage = true;

    @ConfigEntry.Gui.Tooltip
    public List<String> blacklist = new ArrayList<>();

}