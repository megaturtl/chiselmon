package cc.turtl.chiselmon.fabric.services;

import cc.turtl.chiselmon.platform.IPathFinder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;
import java.util.Optional;

public class PathFinderFabric implements IPathFinder {
    @Override
    public Optional<Path> getModPath(String modId, String path) {
        return FabricLoader.getInstance().getModContainer(modId).flatMap(container -> container.findPath(path));
    }

    @Override
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
