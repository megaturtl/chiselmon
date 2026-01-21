package cc.turtl.chiselmon.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cc.turtl.chiselmon.Chiselmon;
import cc.turtl.chiselmon.WorldDataStore;
import cc.turtl.chiselmon.feature.checkspawntracker.CheckSpawnTrackerConfig;
import cc.turtl.chiselmon.feature.eggpreview.EggPreviewConfig;
import cc.turtl.chiselmon.feature.spawnalert.SpawnAlertConfig;
import cc.turtl.chiselmon.feature.spawnlogger.SpawnLoggerConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = Chiselmon.MODID)
public class ModConfig implements ConfigData {

    @ConfigEntry.Gui.Tooltip
    public boolean modDisabled = false;

    @ConfigEntry.Gui.TransitiveObject
    @ConfigEntry.Category("pc")
    public PcConfig pc = new PcConfig();

    @ConfigEntry.Gui.TransitiveObject
    @ConfigEntry.Category("pc")
    public EggPreviewConfig eggPreview = new EggPreviewConfig();

    @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
    public ThresholdConfig threshold = new ThresholdConfig();

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Category("hud")
    public boolean showPokeRodBaitAboveHotbar = true;

    @ConfigEntry.Gui.CollapsibleObject(startExpanded = false)
    @ConfigEntry.Category("hud")
    public CheckSpawnTrackerConfig checkSpawnTracker = new CheckSpawnTrackerConfig();

    @ConfigEntry.Gui.TransitiveObject
    @ConfigEntry.Category("spawnAlert")
    public SpawnAlertConfig spawnAlert = new SpawnAlertConfig();

    @ConfigEntry.Gui.TransitiveObject
    @ConfigEntry.Category("spawnLogger")
    public SpawnLoggerConfig spawnLogger = new SpawnLoggerConfig();

    @ConfigEntry.Gui.Tooltip
    public boolean debugMode = false;

    // Hidden data stores!! Cannot be directly accessed in the config menu by the
    // player
    @ConfigEntry.Gui.Excluded
    public Map<String, WorldDataStore> worldDataMap = new ConcurrentHashMap<>();

    // custom validation to run on save and load
    @Override
    public void validatePostLoad() {
        checkSpawnTracker.validatePostLoad();
    }
}