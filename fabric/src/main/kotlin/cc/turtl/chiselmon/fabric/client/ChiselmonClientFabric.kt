package cc.turtl.chiselmon.fabric.client

import cc.turtl.chiselmon.BuildDetails
import cc.turtl.chiselmon.ChiselmonPacks
import cc.turtl.chiselmon.client.ChiselmonClientCommon
import cc.turtl.chiselmon.util.MiscUtil
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.ResourcePackActivationType
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.chat.Component

object ChiselmonClientFabric : ClientModInitializer {
    override fun onInitializeClient() {
        ChiselmonClientCommon.init()
        registerPacks()
    }

    private fun registerPacks() {
        val modContainer = FabricLoader.getInstance()
            .getModContainer(BuildDetails.MOD_ID)
            .orElseThrow()

        for (pack in ChiselmonPacks.BuiltInPack.ALL) {
            // Check if required mods are loaded
            val shouldLoad = pack.requiredModIds.stream()
                .allMatch { modId: String? -> FabricLoader.getInstance().isModLoaded(modId) }

            if (shouldLoad) {
                ResourceManagerHelper.registerBuiltinResourcePack(
                    MiscUtil.modResource(pack.id),
                    modContainer,
                    Component.literal(pack.name),
                    ResourcePackActivationType.DEFAULT_ENABLED
                )
            }
        }
    }
}