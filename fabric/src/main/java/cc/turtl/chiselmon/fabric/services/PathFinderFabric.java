package cc.turtl.chiselmon.fabric.services;

import cc.turtl.chiselmon.services.IPathFinder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;
import java.util.Optional;

public class PathFinderFabric implements IPathFinder {
    @Override
    public Optional<Path> getPath(String modId, String path) {
        return FabricLoader.getInstance().getModContainer(modId).flatMap(container -> container.findPath(path));
    }
}
