package cc.turtl.chiselmon.module.feature;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import cc.turtl.chiselmon.feature.spawnalert.SpawnAlertCommand;
import cc.turtl.chiselmon.feature.spawnalert.SpawnAlertFeature;
import cc.turtl.chiselmon.module.ChiselmonModule;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class SpawnAlertModule implements ChiselmonModule {
    private final SpawnAlertFeature feature;

    public SpawnAlertModule(SpawnAlertFeature feature) {
        this.feature = feature;
    }

    @Override
    public String id() {
        return "spawn-alert";
    }

    @Override
    public void initialize() {
        feature.initialize();
    }

    @Override
    public void registerCommands(LiteralArgumentBuilder<FabricClientCommandSource> root) {
        root.then(SpawnAlertCommand.register());
    }
}
