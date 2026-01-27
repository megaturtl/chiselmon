package cc.turtl.chiselmon.neoforge;

import cc.turtl.chiselmon.Chiselmon;
import net.neoforged.fml.common.Mod;

@Mod(Chiselmon.MOD_ID)
public final class ChiselmonNeoForge {
    public ChiselmonNeoForge() {
        // Run our common setup.
        Chiselmon.init();
    }
}
