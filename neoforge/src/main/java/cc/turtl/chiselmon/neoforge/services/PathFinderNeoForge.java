package cc.turtl.chiselmon.neoforge.services;

import cc.turtl.chiselmon.platform.IPathFinder;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.moddiscovery.locators.NeoForgeDevProvider;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;

import java.nio.file.Path;
import java.util.Optional;

public class PathFinderNeoForge implements IPathFinder {
    @Override
    public Optional<Path> getModPath(String modId, String path) {
        return Optional.ofNullable(ModList.get().getModFileById(modId))
                .map(info -> info.getFile().findResource(path));
    }

    @Override
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }
}
