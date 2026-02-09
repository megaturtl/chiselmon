package cc.turtl.chiselmon.system.group;

import cc.turtl.chiselmon.ChiselmonConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class PokemonGroupRegistry {
    private static final Logger LOGGER = LogManager.getLogger(ChiselmonConstants.MOD_ID);
    private final Map<String, PokemonGroup> groups = new HashMap<>();

    public PokemonGroupRegistry() {
        LOGGER.info("[PokemonGroupRegistry] Constructor called, calling reset()");
        this.reset();
    }

    public void register(PokemonGroup group) {
        LOGGER.info("[PokemonGroupRegistry] Registering group: {}", group.id());
        groups.put(group.id(), group);
    }

    public void unregister(String id) {
        groups.remove(id);
    }

    /**
     * Resets registry to only include hardcoded defaults.
     */
    public void reset() {
        LOGGER.info("[PokemonGroupRegistry] reset() called");
        groups.clear();
        List.of(
                DefaultPokemonGroups.SHINIES,
                DefaultPokemonGroups.LEGENDARIES,
                DefaultPokemonGroups.EXTREME_SIZES
        ).forEach(this::register);
        LOGGER.info("[PokemonGroupRegistry] reset() finished, groups count: {}", groups.size());
    }

    public Optional<PokemonGroup> get(String id) {
        return Optional.ofNullable(groups.get(id));
    }

    /**
     * Returns all groups sorted by priority (Highest first).
     */
    public List<PokemonGroup> getSorted() {
        List<PokemonGroup> list = new ArrayList<>(groups.values());
        list.sort(Comparator.comparing(PokemonGroup::priority));
        LOGGER.info("[PokemonGroupRegistry] getSorted() returning {} groups", list.size());
        return list;
    }
}