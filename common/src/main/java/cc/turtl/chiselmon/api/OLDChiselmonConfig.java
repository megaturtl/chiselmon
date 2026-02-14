package cc.turtl.chiselmon.api;

import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.leveldata.PersistentLevelData;
import cc.turtl.chiselmon.system.alert.AlertConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Config(name = ChiselmonConstants.MOD_ID)
public class OLDChiselmonConfig implements ConfigData {

    @ConfigEntry.Gui.Tooltip
    public boolean modDisabled = false;

    @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
    public ThresholdConfig threshold = new ThresholdConfig();

    @ConfigEntry.Gui.TransitiveObject
    @ConfigEntry.Category("pc")
    public OLDPCConfig pc = new OLDPCConfig();

    @ConfigEntry.Gui.TransitiveObject
    @ConfigEntry.Category("alert")
    public AlertConfig alert = new AlertConfig();

    // Storing basic info per level/server in the config for now (I am aware of the jankiness :sob:)
    @ConfigEntry.Gui.Excluded
    public Map<String, PersistentLevelData> levelDataMap = new ConcurrentHashMap<>();

    public static class ThresholdConfig implements ConfigData {
        @ConfigEntry.Gui.Tooltip
        public float extremeSmall = 0.4F;

        @ConfigEntry.Gui.Tooltip
        public float extremeLarge = 1.6F;

        @ConfigEntry.Gui.Tooltip
        public int maxIvs = 5;
    }
}