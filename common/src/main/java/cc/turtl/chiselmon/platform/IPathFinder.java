package cc.turtl.chiselmon.platform;

import java.nio.file.Path;
import java.util.Optional;

public interface IPathFinder {

    Optional<Path> getPath(String modId, String path);
}