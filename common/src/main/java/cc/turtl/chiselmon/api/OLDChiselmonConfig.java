package cc.turtl.chiselmon.api;

import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.system.alert.AlertConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = ChiselmonConstants.MOD_ID)
public class OLDChiselmonConfig implements ConfigData {

    @ConfigEntry.Gui.TransitiveObject
    @ConfigEntry.Category("alert")
    public AlertConfig alert = new AlertConfig();
}