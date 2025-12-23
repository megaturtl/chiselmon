package cc.turtl.cobbleaid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cc.turtl.cobbleaid.feature.pc.sort.PokemonCustomSortMode;
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

    // Hidden data stores!! Cannot be directly accessed in the config menu by the
    // player
    @ConfigEntry.Gui.Excluded
    public Map<String, WorldDataStore> worldDataMap = new ConcurrentHashMap<>();

    // custom validation to run on save and load
    @Override
    public void validatePostLoad() {
        spawnTracker.validatePostLoad();
    }

    public static class PcConfig implements ConfigData {

        @ConfigEntry.Gui.Tooltip
        public boolean highlightHA = false;

        @ConfigEntry.Gui.Tooltip
        public boolean quickSortEnabled = false;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.DROPDOWN)
        public PokemonCustomSortMode quickSortMode = PokemonCustomSortMode.POKEDEX_NUMBER;

        @ConfigEntry.Gui.Tooltip
        public boolean bookmarksEnabled = true;

        @ConfigEntry.Gui.Tooltip
        public boolean showEggPreview = false;

        @ConfigEntry.Gui.CollapsibleObject(startExpanded = false)
        public PcTooltipConfig tooltip = new PcTooltipConfig();

        @ConfigEntry.Gui.CollapsibleObject(startExpanded = false)
        public PcIconConfig icons = new PcIconConfig();

        public static class PcTooltipConfig implements ConfigData {
            @ConfigEntry.Gui.Tooltip
            public boolean showTooltips = false;

            @ConfigEntry.Gui.Tooltip
            public boolean showDetailedTooltipOnShift = false;
        }

        public static class PcIconConfig implements ConfigData {
            @ConfigEntry.Gui.Tooltip
            public boolean hiddenAbility = true;

            @ConfigEntry.Gui.Tooltip
            public boolean highIvs = true;

            @ConfigEntry.Gui.Tooltip
            public boolean shiny = true;

            @ConfigEntry.Gui.Tooltip
            public boolean extremeSize = true;

            @ConfigEntry.Gui.Tooltip
            public boolean rideable = false;
        }
    }

    public static class ThresholdConfig implements ConfigData {
        @ConfigEntry.Gui.Tooltip
        public float extremeSmall = 0.5F;

        @ConfigEntry.Gui.Tooltip
        public float extremeLarge = 1.5F;

        @ConfigEntry.Gui.Tooltip
        public int maxIvs = 5;
    }

    public static class SpawnTrackerConfig implements ConfigData {

        @ConfigEntry.Gui.Excluded
        private static final int DEFAULT_POLL_TICK_INTERVAL = 60;
        @ConfigEntry.Gui.Excluded
        private static final int MIN_POLL_TICK_INTERVAL = 20;
        @ConfigEntry.Gui.Excluded
        private static final int MAX_POLL_TICK_INTERVAL = 200;

        @ConfigEntry.Gui.Tooltip
        public boolean enabled = false;

        @ConfigEntry.BoundedDiscrete(min = MIN_POLL_TICK_INTERVAL, max = MAX_POLL_TICK_INTERVAL)
        @ConfigEntry.Gui.Tooltip
        public int pollTickInterval = DEFAULT_POLL_TICK_INTERVAL;

        @ConfigEntry.Gui.Tooltip
        public String bucket = "common";

        @ConfigEntry.Gui.Tooltip
        public List<String> trackedPokemon = new ArrayList<>(Arrays.asList(
                "Sentret",
                "Fomantis",
                "Bidoof"));

        @Override
        public void validatePostLoad() {
            if (pollTickInterval < MIN_POLL_TICK_INTERVAL || pollTickInterval > MAX_POLL_TICK_INTERVAL) {
                pollTickInterval = DEFAULT_POLL_TICK_INTERVAL;
            }
        }
    }
}