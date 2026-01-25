package cc.turtl.chiselmon;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cc.turtl.chiselmon.module.feature.checkspawntracker.CheckSpawnTrackerConfig;
import cc.turtl.chiselmon.module.feature.eggpreview.EggPreviewConfig;
import cc.turtl.chiselmon.module.feature.pc.PcConfig;
import cc.turtl.chiselmon.module.feature.spawnalert.SpawnAlertConfig;
import cc.turtl.chiselmon.module.feature.spawnlogger.SpawnLoggerConfig;
import cc.turtl.chiselmon.service.WorldDataService.WorldDataStore;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = ChiselmonConstants.MODID)
public class ChiselmonConfig implements ConfigData {

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

    public class ThresholdConfig implements ConfigData {
    @ConfigEntry.Gui.Tooltip
    public float extremeSmall = 0.4F;

    @ConfigEntry.Gui.Tooltip
    public float extremeLarge = 1.6F;

    @ConfigEntry.Gui.Tooltip
    public int maxIvs = 5;
}
}