package cc.turtl.chiselmon.module.feature;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import cc.turtl.chiselmon.feature.spawnlogger.SpawnLoggerCommand;
import cc.turtl.chiselmon.feature.spawnlogger.SpawnLoggerFeature;
import cc.turtl.chiselmon.module.ChiselmonModule;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class SpawnLoggerModule implements ChiselmonModule {
    private final SpawnLoggerFeature feature;

    public SpawnLoggerModule(SpawnLoggerFeature feature) {
        this.feature = feature;
    }

    @Override
    public String id() {
        return "spawn-logger";
    }

    @Override
    public void initialize() {
        feature.initialize();
    }

    @Override
    public void registerCommands(LiteralArgumentBuilder<FabricClientCommandSource> root) {
        root.then(SpawnLoggerCommand.register());
    }
}
