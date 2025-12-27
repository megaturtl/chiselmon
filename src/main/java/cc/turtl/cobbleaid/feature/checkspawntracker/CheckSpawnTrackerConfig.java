package cc.turtl.cobbleaid.feature.checkspawntracker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class CheckSpawnTrackerConfig implements ConfigData {

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
    public List<String> trackedPokemon = new ArrayList<>(Arrays.asList());

    @Override
    public void validatePostLoad() {
        if (pollTickInterval < MIN_POLL_TICK_INTERVAL || pollTickInterval > MAX_POLL_TICK_INTERVAL) {
            pollTickInterval = DEFAULT_POLL_TICK_INTERVAL;
        }
    }
}