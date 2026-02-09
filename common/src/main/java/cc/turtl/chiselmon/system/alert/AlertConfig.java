package cc.turtl.chiselmon.system.alert;

import cc.turtl.chiselmon.ChiselmonSystems;
import cc.turtl.chiselmon.system.group.PokemonGroup;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.ArrayList;
import java.util.List;

public class AlertConfig implements ConfigData {

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
    public List<GroupAlertEntry> groups = new ArrayList<>();

    @Override
    public void validatePostLoad() {
        // Clamp master volume
        this.masterVolume = Math.max(0, Math.min(100, this.masterVolume));

        // Sync with registry - add missing groups
        for (PokemonGroup group : ChiselmonSystems.pokemonGroups().getRegistry().getSorted()) {
            if (groups.stream().noneMatch(entry -> entry.groupId.equals(group.id()))) {
                groups.add(new GroupAlertEntry(group.id()));
            }
        }

        // Validate each group config
        for (GroupAlertEntry entry : groups) {
            entry.validatePostLoad();
        }
    }

    public GroupAlertConfig getForGroup(String id) {
        return groups.stream()
                .filter(entry -> entry.groupId.equals(id))
                .findFirst()
                .map(entry -> entry.config)
                .orElseGet(GroupAlertConfig::new);
    }

    public static class GroupAlertEntry implements ConfigData {

        @ConfigEntry.Gui.Tooltip
        public String groupId = "";

        @ConfigEntry.Gui.CollapsibleObject
        public GroupAlertConfig config = new GroupAlertConfig();

        public GroupAlertEntry() {}

        public GroupAlertEntry(String groupId) {
            this.groupId = groupId;
        }

        @Override
        public void validatePostLoad() {
            config.validatePostLoad();
        }
    }

    public static class GroupAlertConfig implements ConfigData {

        public boolean enabled = false;

        public boolean playSound = false;

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