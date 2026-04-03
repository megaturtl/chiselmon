package cc.turtl.chiselmon;

import cc.turtl.chiselmon.api.filter.FiltersUserData;
import cc.turtl.chiselmon.api.storage.ScopedData;
import cc.turtl.chiselmon.api.storage.StorageScope;
import cc.turtl.chiselmon.api.storage.adapter.GsonAdapter;
import cc.turtl.chiselmon.api.storage.adapter.H2Adapter;
import cc.turtl.chiselmon.feature.pc.PCUserData;
import cc.turtl.chiselmon.system.tracker.EncounterDatabase;
import cc.turtl.turtlshell.api.client.ClientEvents;
import kotlin.Unit;

import java.util.List;

/**
 * Central registry of all scoped data storage.
 */
public class ChiselmonStorage {
    public static final ScopedData<FiltersUserData> FILTERS = new ScopedData<>(
            GsonAdapter.of("filters.json", FiltersUserData.class, FiltersUserData::withDefaults)
    );
    public static final ScopedData<PCUserData> PC_SETTINGS = new ScopedData<>(
            GsonAdapter.of("pc.json", PCUserData.class, PCUserData::new)
    );
    public static final ScopedData<EncounterDatabase> ENCOUNTERS = new ScopedData<>(
            H2Adapter.of("encounters", EncounterDatabase::new, EncounterDatabase::flush, EncounterDatabase::close)
    );

    private static final List<ScopedData<?>> ALL = List.of(FILTERS, PC_SETTINGS, ENCOUNTERS);

    private static final int AUTOSAVE_INTERVAL_TICKS = 20 * 60 * 5;
    private static int tickCount = 0;

    /**
     * Registers auto saving + clear on disconnect.
     */
    public static void init() {

        // Save + clear world-scoped data on world leave
        ClientEvents.INSTANCE.getLEVEL_DISCONNECTED().subscribe(e -> {
            StorageScope world = StorageScope.currentWorld();
            if (world != null) {
                for (ScopedData<?> data : ALL) {
                    data.saveAndClear(world);
                }
            }
            return Unit.INSTANCE;
        });

        // Save everything on game close
        ClientEvents.INSTANCE.getGAME_STOPPING().subscribe(e -> {
            saveAll();
            return Unit.INSTANCE;
        });

        // Autosave every 5 minutes (only while in a world)
        ClientEvents.INSTANCE.getTICK_POST().subscribe(e -> {
            if (++tickCount >= AUTOSAVE_INTERVAL_TICKS) {
                tickCount = 0;
                saveAll();
            }
            return Unit.INSTANCE;
        });
    }

    /**
     * Saves all currently loaded data across all scopes.
     */
    public static void saveAll() {
        for (ScopedData<?> data : ALL) {
            data.saveAll();
        }
    }
}