package cc.turtl.chiselmon.neoforge.services;

import cc.turtl.chiselmon.platform.IPathFinder;
import net.neoforged.fml.ModList;

import java.nio.file.Path;
import java.util.Optional;

public class PathFinderNeoForge implements IPathFinder {
    @Override
    public Optional<Path> getPath(String modId, String path) {
        return Optional.ofNullable(ModList.get().getModFileById(modId))
                .map(info -> info.getFile().findResource(path));
    }
}
