package cc.turtl.chiselmon.feature.spawnalert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class SpawnAlertConfig implements ConfigData {

    @ConfigEntry.Gui.Excluded
    private static final int MIN_VOLUME = 0;
    @ConfigEntry.Gui.Excluded
    private static final int MAX_VOLUME = 100;

    @ConfigEntry.Gui.Excluded
    private static final int MIN_SOUND_DELAY = 5;
    @ConfigEntry.Gui.Excluded
    private static final int MAX_SOUND_DELAY = 100;

    @ConfigEntry.Gui.Tooltip
    public boolean enabled = false;

    @ConfigEntry.Gui.Tooltip
    public boolean alertOnShiny = true;

    @ConfigEntry.Gui.Tooltip
    public boolean alertOnLegendary = true;

    @ConfigEntry.Gui.Tooltip
    public boolean alertOnUltraBeast = true;

    @ConfigEntry.Gui.Tooltip
    public boolean alertOnParadox = true;

    @ConfigEntry.Gui.Tooltip
    public boolean alertOnExtremeSize = false;

    @ConfigEntry.Gui.Tooltip
    public boolean alertOnCustomList = true;

    @ConfigEntry.Gui.Tooltip
    public List<String> whitelist = new ArrayList<>(Arrays.asList());

    @ConfigEntry.Gui.Tooltip
    public List<String> blacklist = new ArrayList<>(Arrays.asList());

    @ConfigEntry.BoundedDiscrete(min = MIN_VOLUME, max = MAX_VOLUME)
    @ConfigEntry.Gui.Tooltip
    public int soundVolume = 100;

    @ConfigEntry.BoundedDiscrete(min = MIN_SOUND_DELAY, max = MAX_SOUND_DELAY)
    @ConfigEntry.Gui.Tooltip
    public int soundDelay = 20;

    @ConfigEntry.Gui.Tooltip
    public boolean highlightEntity = false;

    @Override
    public void validatePostLoad() throws ValidationException {
        if (soundVolume > MAX_VOLUME) {
            soundVolume = 100;
        }
        if (soundVolume < MIN_VOLUME) {
            soundVolume = 0;
        }
    }
}