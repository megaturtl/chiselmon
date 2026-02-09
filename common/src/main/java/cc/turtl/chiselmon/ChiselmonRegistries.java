package cc.turtl.chiselmon;

import cc.turtl.chiselmon.api.data.species.ClientSpeciesRegistry;

/**
 * Central access point for all registries.
 */
public final class ChiselmonRegistries {
    private static ClientSpeciesRegistry species;

    private ChiselmonRegistries() {}

    static void init() {
        species = new ClientSpeciesRegistry();
    }

    public static ClientSpeciesRegistry species() {
        return species;
    }
}