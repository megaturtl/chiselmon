package cc.turtl.chiselmon;

import cc.turtl.chiselmon.system.group.PokemonGroupSystem;
import cc.turtl.chiselmon.system.tracker.PokemonTrackerSystem;
import cc.turtl.chiselmon.system.alert.PokemonAlertSystem;

/**
 * Central access point for all features/systems.
 */
public final class ChiselmonSystems {
    private static PokemonTrackerSystem pokemonTrackerSystem;
    private static PokemonAlertSystem pokemonAlertSystem;
    private static PokemonGroupSystem pokemonGroupSystem;

    private ChiselmonSystems() {}

    static void init() {
        pokemonTrackerSystem = new PokemonTrackerSystem();
        pokemonAlertSystem = new PokemonAlertSystem(pokemonTrackerSystem);
        pokemonGroupSystem = new PokemonGroupSystem();
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