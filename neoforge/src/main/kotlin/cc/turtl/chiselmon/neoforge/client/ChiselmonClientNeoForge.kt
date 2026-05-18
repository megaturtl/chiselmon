package cc.turtl.chiselmon.neoforge.client

import cc.turtl.chiselmon.BuildDetails
import cc.turtl.chiselmon.core.ChiselmonConstants
import cc.turtl.chiselmon.core.ChiselmonCoreCommon
import cc.turtl.chiselmon.client.ChiselmonPacks
import cc.turtl.chiselmon.client.ChiselmonClientCommon
import cc.turtl.chiselmon.client.config.ChiselmonConfig
import cc.turtl.chiselmon.core.util.modResource
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.repository.FolderRepositorySource
import net.minecraft.server.packs.repository.Pack
import net.minecraft.server.packs.repository.PackSource
import net.minecraft.world.level.validation.DirectoryValidator
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.ModContainer
import net.neoforged.fml.ModList
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.client.gui.IConfigScreenFactory
import net.neoforged.neoforge.event.AddPackFindersEvent
import java.nio.file.Path

// Make sure there's just one @Mod entrypoint per modId on NeoForge
// KFF's KotlinModContainer.constructMod() runs AutoKotlinEventBusSubscriber.inject()
// once per @Mod class for the modId, which double-registers every @EventBusSubscriber listener.
@EventBusSubscriber
@Mod(value = BuildDetails.MOD_ID, dist = [Dist.CLIENT])
object ChiselmonClientNeoForge {
    init {
        ChiselmonCoreCommon.init()
        ChiselmonClientCommon.init()
        registerConfigScreen()
    }

    private fun registerConfigScreen() {
        ModList.get().getModContainerById(BuildDetails.MOD_ID).orElse(null)?.let { c ->
            c.registerExtensionPoint(
                IConfigScreenFactory::class.java,
                IConfigScreenFactory { _: ModContainer, parent: Screen ->
                    ChiselmonConfig.createScreen(parent)
                })
        }
    }

    @SubscribeEvent
    fun onAddPackFinders(event: AddPackFindersEvent) {
        if (event.packType != PackType.CLIENT_RESOURCES) return

        // Add Chiselmon builtins
        for (pack in ChiselmonPacks.BuiltInPack.ALL) {
            if (pack.requiredModIds.all { ModList.get().isLoaded(it) }) {
                event.addPackFinders(
                    modResource("resourcepacks/" + pack.id),
                    PackType.CLIENT_RESOURCES,
                    Component.literal(pack.name),
                    PackSource.BUILT_IN,
                    false,
                    Pack.Position.TOP
                )
            }
        }

        // Create the Custom Wallpapers pack structure
        ChiselmonPacks.getOrCreateCustomWallpaperDir()
        // Add config folder as a pack source
        event.addRepositorySource(
            FolderRepositorySource(
                ChiselmonConstants.CONFIG_PATH,
                PackType.CLIENT_RESOURCES,
                PackSource.BUILT_IN,
                DirectoryValidator { _: Path? -> true }
            )
        )
    }
}

