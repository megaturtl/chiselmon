package cc.turtl.chiselmon.system.alert;

import cc.turtl.chiselmon.ChiselmonConstants;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class AlertConfig implements ConfigData {
    private static final Logger LOGGER = LogManager.getLogger(ChiselmonConstants.MOD_ID);

    @ConfigEntry.Gui.Tooltip
    public boolean masterEnabled = true;

    @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
    @ConfigEntry.Gui.Tooltip
    public int masterVolume = 100;

    @ConfigEntry.Gui.Tooltip
    public boolean showFormInMessage = true;

    @ConfigEntry.Gui.Tooltip
    public List<String> blacklist = new ArrayList<>();

    @ConfigEntry.Gui.CollapsibleObject(startExpanded = false)
    public GroupAlertConfig legendaries = new GroupAlertConfig();

    @ConfigEntry.Gui.CollapsibleObject(startExpanded = false)
    public GroupAlertConfig shinies = new GroupAlertConfig();

    @ConfigEntry.Gui.CollapsibleObject(startExpanded = false)
    public GroupAlertConfig extreme_sizes = new GroupAlertConfig();

    @Override
    public void validatePostLoad() {
        LOGGER.info("[AlertConfig] validatePostLoad() called");
        // Clamp master volume
        this.masterVolume = Math.max(0, Math.min(100, this.masterVolume));

        // Validate each group config
        legendaries.validatePostLoad();
        shinies.validatePostLoad();
        extreme_sizes.validatePostLoad();
        LOGGER.info("[AlertConfig] validatePostLoad() finished");
    }

    /**
     * Gets the alert config for a specific group ID.
     */
    public GroupAlertConfig getForGroup(String id) {
        return switch (id) {
            case "legendaries" -> legendaries;
            case "shinies" -> shinies;
            case "extreme_sizes" -> extreme_sizes;
            default -> new GroupAlertConfig();
        };
    }

    public static class GroupAlertConfig implements ConfigData {

        public boolean enabled = true;

        public boolean playSound = true;

        public boolean sendChatMessage = true;

        public boolean highlightEntity = true;

        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public AlertSounds sound = AlertSounds.EXP_ORB;

        @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
        public int volume = 100;

        @ConfigEntry.BoundedDiscrete(min = 0, max = 200)
        public int pitch = 100;

        @Override
        public void validatePostLoad() {
            this.volume = Math.max(0, Math.min(100, this.volume));
            this.pitch = Math.max(0, Math.min(200, this.pitch));
        }
    }
}