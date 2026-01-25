package cc.turtl.chiselmon.module.feature.spawnalert;

import java.util.ArrayList;
import java.util.List;

import cc.turtl.chiselmon.util.ColorUtil;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class SpawnAlertConfig implements ConfigData {

    @ConfigEntry.Gui.Excluded
    private static final int MIN_VOLUME = 0;
    @ConfigEntry.Gui.Excluded
    private static final int MAX_VOLUME = 100;

    @ConfigEntry.Gui.Tooltip
    public boolean enabled = false;

    @ConfigEntry.Gui.Tooltip
    public boolean suppressPlushies = true;

    @ConfigEntry.Gui.Tooltip
    public boolean showFormInMessage = true;

    @ConfigEntry.BoundedDiscrete(min = MIN_VOLUME, max = MAX_VOLUME)
    @ConfigEntry.Gui.Tooltip
    public int masterVolume = 100;

    @ConfigEntry.Gui.CollapsibleObject(startExpanded = false)
    public AlertConfig legendary = new AlertConfig(true, true, ColorUtil.MAGENTA);

    @ConfigEntry.Gui.CollapsibleObject(startExpanded = false)
    public AlertConfig shiny = new AlertConfig(true, true, ColorUtil.GOLD);

    @ConfigEntry.Gui.CollapsibleObject(startExpanded = false)
    public AlertConfig size = new AlertConfig(true, false, ColorUtil.TEAL);

    @ConfigEntry.Gui.CollapsibleObject(startExpanded = false)
    public AlertConfig list = new AlertConfig(true, false, ColorUtil.WHITE);

    @ConfigEntry.Gui.Tooltip
    public List<String> whitelist = new ArrayList<>();
    @ConfigEntry.Gui.Tooltip
    public List<String> blacklist = new ArrayList<>();

    @ConfigEntry.Gui.Tooltip
    public boolean despawnTrackEnabled = false;

    @Override
    public void validatePostLoad() throws ValidationException {
        masterVolume = clampVolume(masterVolume);
    }

    private static int clampVolume(int volume) {
        if (volume > MAX_VOLUME) return 100;
        if (volume < MIN_VOLUME) return 0;
        return volume;
    }
    public static class AlertConfig implements ConfigData {

        @ConfigEntry.Gui.TransitiveObject
        public boolean enabled;
        @ConfigEntry.Gui.TransitiveObject
        public boolean playSound;
        @ConfigEntry.Gui.TransitiveObject
        public boolean sendChatMessage = true;
        @ConfigEntry.Gui.TransitiveObject
        public boolean highlightEntity = true;

        @ConfigEntry.Gui.TransitiveObject
        @ConfigEntry.BoundedDiscrete(min = MIN_VOLUME, max = MAX_VOLUME)
        public int volume = 100;

        @ConfigEntry.Gui.TransitiveObject
        @ConfigEntry.ColorPicker
        public int highlightColor;

        public AlertConfig() {
            this(true, false, ColorUtil.WHITE);
        }

        public AlertConfig(boolean enabled, boolean playSound, int highlightColor) {
            this.enabled = enabled;
            this.playSound = playSound;
            this.highlightColor = highlightColor;
        }

        @Override
        public void validatePostLoad() throws ValidationException {
            volume = clampVolume(volume);
        }
    }
}