package cc.turtl.chiselmon.module;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public interface ChiselmonModule {
    String id();

    void initialize();

    default void registerCommands(LiteralArgumentBuilder<FabricClientCommandSource> root) {
    }
}
