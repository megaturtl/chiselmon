package cc.turtl.chiselmon.feature.checkspawntracker;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CheckSpawnTrackerConfig implements ConfigData {

    @ConfigEntry.Gui.Excluded
    private static final int DEFAULT_POLL_TICK_INTERVAL = 60;
    @ConfigEntry.Gui.Excluded
    private static final int MIN_POLL_TICK_INTERVAL = 20;
    @ConfigEntry.Gui.Excluded
    private static final int MAX_POLL_TICK_INTERVAL = 200;
    @ConfigEntry.Gui.Tooltip
    public boolean enabled = false;
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = MIN_POLL_TICK_INTERVAL, max = MAX_POLL_TICK_INTERVAL)
    public int pollTickInterval = DEFAULT_POLL_TICK_INTERVAL;
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public RarityBucket bucket = RarityBucket.COMMON;
    @ConfigEntry.Gui.Tooltip
    public List<String> trackedPokemon = new ArrayList<>(List.of());

    @Override
    public void validatePostLoad() {
        if (pollTickInterval < MIN_POLL_TICK_INTERVAL || pollTickInterval > MAX_POLL_TICK_INTERVAL) {
            pollTickInterval = DEFAULT_POLL_TICK_INTERVAL;
        }
    }

    public enum RarityBucket {
        COMMON("common", "Common"),
        UNCOMMON("uncommon", "Uncommon"),
        RARE("rare", "Rare"),
        ULTRA_RARE("ultra-rare", "Ultra Rare");

        private final String id;
        private final String displayName;

        RarityBucket(String id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }

        public String getId() {
            return id;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }
}