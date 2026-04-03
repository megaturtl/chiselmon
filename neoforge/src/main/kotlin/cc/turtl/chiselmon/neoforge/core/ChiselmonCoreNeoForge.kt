package cc.turtl.chiselmon.neoforge.core

import cc.turtl.chiselmon.BuildDetails
import cc.turtl.chiselmon.core.ChiselmonCoreCommon
import net.neoforged.fml.common.Mod

@Mod(BuildDetails.MOD_ID)
object ChiselmonCoreNeoForge {
    init {
        ChiselmonCoreCommon.init()
    }
}