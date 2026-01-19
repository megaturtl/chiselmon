package cc.turtl.chiselmon.feature.spawnalert;

import java.util.ArrayList;
import java.util.Arrays;
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
    public LegendaryAlertConfig legendary = new LegendaryAlertConfig();

    @ConfigEntry.Gui.CollapsibleObject(startExpanded = false)
    public ShinyAlertConfig shiny = new ShinyAlertConfig();

    @ConfigEntry.Gui.CollapsibleObject(startExpanded = false)
    public SizeAlertConfig size = new SizeAlertConfig();

    @ConfigEntry.Gui.CollapsibleObject(startExpanded = false)
    public ListAlertConfig list = new ListAlertConfig();

    @ConfigEntry.Gui.Tooltip
    public List<String> blacklist = new ArrayList<>(Arrays.asList());

    @Override
    public void validatePostLoad() throws ValidationException {
        if (masterVolume > MAX_VOLUME) {
            masterVolume = 100;
        }
        if (masterVolume < MIN_VOLUME) {
            masterVolume = 0;
        }
    }

    // legendary/ub/mythical
    public class LegendaryAlertConfig implements ConfigData {

        public boolean enabled = true;

        public boolean sendChatMessage = true;

        public boolean playSound = true;

        @ConfigEntry.BoundedDiscrete(min = MIN_VOLUME, max = MAX_VOLUME)
        public int volume = 100;

        public boolean highlightEntity = false;

        @ConfigEntry.ColorPicker
        public int highlightColor = ColorUtil.MAGENTA;

        @Override
        public void validatePostLoad() throws ValidationException {
            if (volume > MAX_VOLUME) {
                volume = 100;
            }
            if (volume < MIN_VOLUME) {
                volume = 0;
            }
        }
    }

    public class ShinyAlertConfig implements ConfigData {

        public boolean enabled = true;

        public boolean sendChatMessage = true;

        public boolean playSound = true;

        @ConfigEntry.BoundedDiscrete(min = MIN_VOLUME, max = MAX_VOLUME)
        public int volume = 100;

        public boolean highlightEntity = false;

        @ConfigEntry.ColorPicker
        public int highlightColor = ColorUtil.GOLD;

        @Override
        public void validatePostLoad() throws ValidationException {
            if (volume > MAX_VOLUME) {
                volume = 100;
            }
            if (volume < MIN_VOLUME) {
                volume = 0;
            }
        }
    }

    public class SizeAlertConfig implements ConfigData {

        public boolean enabled = false;

        public boolean sendChatMessage = true;

        public boolean playSound = true;

        @ConfigEntry.BoundedDiscrete(min = MIN_VOLUME, max = MAX_VOLUME)
        public int volume = 100;

        public boolean highlightEntity = false;

        @ConfigEntry.ColorPicker
        public int highlightColor = ColorUtil.TEAL;

        @Override
        public void validatePostLoad() throws ValidationException {
            if (volume > MAX_VOLUME) {
                volume = 100;
            }
            if (volume < MIN_VOLUME) {
                volume = 0;
            }
        }
    }

    public class ListAlertConfig implements ConfigData {

        public boolean enabled = true;

        public boolean sendChatMessage = true;

        public boolean playSound = true;

        @ConfigEntry.BoundedDiscrete(min = MIN_VOLUME, max = MAX_VOLUME)
        public int volume = 100;

        public boolean highlightEntity = false;

        @ConfigEntry.ColorPicker
        public int highlightColor = ColorUtil.WHITE;

        @ConfigEntry.Gui.Tooltip
        public List<String> whitelist = new ArrayList<>(Arrays.asList());

        @Override
        public void validatePostLoad() throws ValidationException {
            if (volume > MAX_VOLUME) {
                volume = 100;
            }
            if (volume < MIN_VOLUME) {
                volume = 0;
            }
        }
    }
}