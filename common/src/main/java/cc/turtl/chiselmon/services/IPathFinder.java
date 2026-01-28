package cc.turtl.chiselmon.services;

import java.nio.file.Path;
import java.util.Optional;

public interface IPathFinder {

    Optional<Path> getPath(String modId, String path);
}