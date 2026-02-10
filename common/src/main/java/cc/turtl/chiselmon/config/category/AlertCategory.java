package cc.turtl.chiselmon.config.category;

import cc.turtl.chiselmon.ChiselmonSystems;
import cc.turtl.chiselmon.config.option.BooleanOption;
import cc.turtl.chiselmon.config.option.ConfigOption;
import cc.turtl.chiselmon.config.option.GroupAlertOption;
import cc.turtl.chiselmon.config.option.IntegerOption;
import cc.turtl.chiselmon.system.group.PokemonGroup;

import java.util.*;

/**
 * Configuration category for alert settings.
 * Supports dynamic group registration - groups are automatically added when registered.
 */
public class AlertCategory implements ConfigCategory {

    // Global alert settings
    public final BooleanOption masterEnabled = BooleanOption.builder()
            .name("masterEnabled")
            .displayName("Master Enable")
            .comment("Enable/disable all alerts globally")
            .defaultValue(true)
            .build();

    public final IntegerOption masterVolume = IntegerOption.builder()
            .name("masterVolume")
            .displayName("Master Volume")
            .comment("Global volume multiplier for all alert sounds (0-100)")
            .defaultValue(100)
            .range(0, 100)
            .build();

    public final BooleanOption showFormInMessage = BooleanOption.builder()
            .name("showFormInMessage")
            .displayName("Show Form in Message")
            .comment("Include the Pokemon's form in alert messages")
            .defaultValue(true)
            .build();

    // Dynamic group alert options - keyed by group ID
    private final Map<String, GroupAlertOption> groupAlerts = new LinkedHashMap<>();

    public AlertCategory() {
        // Initialize default groups
        syncGroupsFromRegistry();
    }

    /**
     * Syncs the group alert options with the current registry.
     * Adds any missing groups and removes any that no longer exist.
     */
    public void syncGroupsFromRegistry() {
        if (ChiselmonSystems.pokemonGroups() == null) {
            return;
        }

        Set<String> registeredIds = new HashSet<>();
        for (PokemonGroup group : ChiselmonSystems.pokemonGroups().getRegistry().getSorted()) {
            registeredIds.add(group.id());
            if (!groupAlerts.containsKey(group.id())) {
                groupAlerts.put(group.id(), new GroupAlertOption(group.id(), group.name()));
            }
        }

        // Remove any groups that are no longer registered (optional - could keep for user customization)
        // groupAlerts.keySet().retainAll(registeredIds);
    }

    /**
     * Gets the alert settings for a specific group.
     * @param groupId The group ID
     * @return The alert option, or a new default one if not found
     */
    public GroupAlertOption getGroupAlert(String groupId) {
        return groupAlerts.computeIfAbsent(groupId, id -> new GroupAlertOption(id, id));
    }

    /**
     * @return All registered group alert options
     */
    public Collection<GroupAlertOption> getGroupAlerts() {
        return Collections.unmodifiableCollection(groupAlerts.values());
    }

    /**
     * @return The group alert map for serialization
     */
    public Map<String, GroupAlertOption> getGroupAlertsMap() {
        return groupAlerts;
    }

    @Override
    public String getName() {
        return "alert";
    }

    @Override
    public String getDisplayName() {
        return "Alerts";
    }

    @Override
    public List<ConfigOption<?>> getOptions() {
        List<ConfigOption<?>> options = new ArrayList<>();
        options.add(masterEnabled);
        options.add(masterVolume);
        options.add(showFormInMessage);
        options.addAll(groupAlerts.values());
        return options;
    }
}
