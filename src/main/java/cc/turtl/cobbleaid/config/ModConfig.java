package cc.turtl.cobbleaid.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.WorldDataStore;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = CobbleAid.MODID)
public class ModConfig implements ConfigData {

    @ConfigEntry.Gui.Tooltip
    public boolean modDisabled = false;
    @ConfigEntry.Gui.Tooltip
    public boolean debugMode = false;

    @ConfigEntry.Gui.TransitiveObject
    @ConfigEntry.Category("pc")
    public PcConfig pc = new PcConfig();

    @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
    public ThresholdConfig threshold = new ThresholdConfig();

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Category("hud")
    public boolean showPokeRodBaitAboveHotbar = true;

    @ConfigEntry.Gui.CollapsibleObject(startExpanded = false)
    @ConfigEntry.Category("hud")
    public SpawnTrackerConfig spawnTracker = new SpawnTrackerConfig();

    @ConfigEntry.Gui.TransitiveObject
    @ConfigEntry.Category("alert")
    public SpawnAlertConfig spawnAlert = new SpawnAlertConfig();    

    // Hidden data stores!! Cannot be directly accessed in the config menu by the
    // player
    @ConfigEntry.Gui.Excluded
    public Map<String, WorldDataStore> worldDataMap = new ConcurrentHashMap<>();

    // custom validation to run on save and load
    @Override
    public void validatePostLoad() {
        spawnTracker.validatePostLoad();
    }
}