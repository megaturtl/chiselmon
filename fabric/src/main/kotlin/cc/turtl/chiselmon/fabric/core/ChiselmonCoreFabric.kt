package cc.turtl.chiselmon.fabric.core

import cc.turtl.chiselmon.core.ChiselmonCoreCommon
import net.fabricmc.api.ModInitializer

object ChiselmonCoreFabric : ModInitializer {
    override fun onInitialize() {
        ChiselmonCoreCommon.init()
    }
}