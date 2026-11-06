package cc.turtl.chiselmon.client

import cc.turtl.chiselmon.BuildDetails
import cc.turtl.chiselmon.client.api.ChiselmonClientEvents
import cc.turtl.chiselmon.client.api.ClientSpeciesRegistry
import cc.turtl.chiselmon.client.command.*
import cc.turtl.chiselmon.client.config.ChiselmonConfig
import cc.turtl.chiselmon.client.feature.CheckSpawnInterceptor
import cc.turtl.chiselmon.client.system.alert.AlertManager
import cc.turtl.chiselmon.client.system.spawnrecorder.SpawnRecorderManager
import cc.turtl.chiselmon.client.system.tracker.TrackerSession
import cc.turtl.chiselmon.core.api.storage.Scope
import cc.turtl.turtlshell.api.client.ClientEvents
import cc.turtl.turtlshell.api.client.keybind.KeybindRegistry
import cc.turtl.turtlshell.api.core.command.CommandRegistry
import net.minecraft.client.Minecraft

object ChiselmonClientCommon {
    fun init() {
        KeybindRegistry.registerGroup(
            category = BuildDetails.MOD_DISPLAY_NAME,
            keybinds = ChiselmonKeybinds.ALL,
        )

        CommandRegistry.registerClientGroup(
            aliases = listOf(BuildDetails.MOD_ID, "ch"),
            commands =
                listOf(
                    InfoCommand(),
                    DebugCommand(),
                    DatabaseCommand(),
                    AlertCommand(),
                    ConfigCommand(),
                    RecordCommand(),
                    DashCommand(),
                ),
        )

        ClientEvents.LEVEL_CONNECTED.subscribe {
            leaveWorld()
            Scope.enterWorld()
            TrackerSession.start()
        }
        // Fabric can disconnect on Netty; dispose client-owned state before feature ticks.
        ClientEvents.TICK_POST.subscribe {
            if (Scope.currentWorld() != null && Minecraft.getInstance().level == null) leaveWorld()
            ClientSpeciesRegistry.tick()
            ChiselmonStorage.tick()
            TrackerSession.tick()
            AlertManager.tick()
            SpawnRecorderManager.session?.tick()
        }
        ClientEvents.GAME_STOPPING.subscribe {
            leaveWorld()
            ChiselmonStorage.saveAndClearAll()
        }

        ChiselmonConfig.init()
        ChiselmonClientEvents.init()
        TrackerSession.init()

        AlertManager.init()
        SpawnRecorderManager.init()

        CheckSpawnInterceptor.init()
        BattleState.init()
    }

    private fun leaveWorld() {
        if (Scope.currentWorld() == null) return
        ClientSpeciesRegistry.onWorldLeave()
        AlertManager.onWorldLeave()
        SpawnRecorderManager.onWorldLeave()
        TrackerSession.stop()
        Scope.leaveWorld()?.let { ChiselmonStorage.saveAndClear(it) }
    }
}
