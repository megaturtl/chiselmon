package cc.turtl.chiselmon.fabric.services;

import cc.turtl.chiselmon.platform.IModChecker;
import net.fabricmc.loader.api.FabricLoader;

public class ModCheckerFabric implements IModChecker {
    @Override
    public boolean isLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }
}