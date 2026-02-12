package cc.turtl.chiselmon.platform;

import java.nio.file.Path;
import java.util.Optional;

public interface IPathFinder {

    Optional<Path> getModPath(String modId, String path);
    Path getConfigDir();
}