package cc.turtl.chiselmon.fabric;

import cc.turtl.chiselmon.Chiselmon;
import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.ChiselmonKeybinds;
import cc.turtl.chiselmon.ChiselmonPacks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;

import static cc.turtl.chiselmon.util.MiscUtil.modResource;

public final class ChiselmonFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EventRegisterFabric.register();
        ChiselmonKeybinds.ALL.forEach(KeyBindingHelper::registerKeyBinding);
        Chiselmon.initClient();
        registerPacks();
    }

    private void registerPacks() {
        var modContainer = FabricLoader.getInstance()
                .getModContainer(ChiselmonConstants.MOD_ID)
                .orElseThrow();

        for (ChiselmonPacks.BuiltInPack pack : ChiselmonPacks.BuiltInPack.ALL) {
            // Check if required mods are loaded
            boolean shouldLoad = pack.requiredModIds().stream()
                    .allMatch(modId -> FabricLoader.getInstance().isModLoaded(modId));

            if (shouldLoad) {
                ResourceManagerHelper.registerBuiltinResourcePack(
                        modResource(pack.id()),
                        modContainer,
                        Component.literal(pack.name()),
                        ResourcePackActivationType.DEFAULT_ENABLED
                );
            }
        }
    }
}