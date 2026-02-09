package cc.turtl.chiselmon;

import cc.turtl.chiselmon.system.group.PokemonGroupSystem;
import cc.turtl.chiselmon.system.tracker.PokemonTrackerSystem;
import cc.turtl.chiselmon.system.alert.PokemonAlertSystem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Central access point for all features/systems.
 */
public final class ChiselmonSystems {
    private static final Logger LOGGER = LogManager.getLogger(ChiselmonConstants.MOD_ID);
    private static PokemonTrackerSystem pokemonTrackerSystem;
    private static PokemonAlertSystem pokemonAlertSystem;
    private static PokemonGroupSystem pokemonGroupSystem;

    private ChiselmonSystems() {}

    static void init() {
        LOGGER.info("[ChiselmonSystems] init() starting");
        pokemonTrackerSystem = new PokemonTrackerSystem();
        LOGGER.info("[ChiselmonSystems] PokemonTrackerSystem created");
        pokemonAlertSystem = new PokemonAlertSystem(pokemonTrackerSystem);
        LOGGER.info("[ChiselmonSystems] PokemonAlertSystem created");
        pokemonGroupSystem = new PokemonGroupSystem();
        LOGGER.info("[ChiselmonSystems] PokemonGroupSystem created, registry groups: {}", 
            pokemonGroupSystem.getRegistry().getSorted().size());
    }

    public static PokemonTrackerSystem pokemonTracker() {
        return pokemonTrackerSystem;
    }

    public static PokemonAlertSystem pokemonAlerter() {
        return pokemonAlertSystem;
    }

    public static PokemonGroupSystem pokemonGroups() {
        return pokemonGroupSystem;
    }
}