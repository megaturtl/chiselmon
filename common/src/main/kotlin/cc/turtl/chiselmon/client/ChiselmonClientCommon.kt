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

        ChiselmonConfig.init()

        ChiselmonClientEvents.init()

        ClientSpeciesRegistry.init()
        ChiselmonStorage.init()

        TrackerSession.init()

        ClientEvents.LEVEL_CONNECTED.subscribe {
            leaveWorld()
            Scope.enterWorld()
            TrackerSession.start()
        }
        ClientEvents.LEVEL_DISCONNECTED.subscribe { leaveWorld() }
        ClientEvents.GAME_STOPPING.subscribe {
            TrackerSession.stop()
            Scope.leaveWorld()
            ChiselmonStorage.saveAndClearAll()
        }

        AlertManager.init()
        SpawnRecorderManager.init()

        CheckSpawnInterceptor.init()
        BattleState.init()
    }

    private fun leaveWorld() {
        TrackerSession.stop()
        Scope.leaveWorld()?.let { ChiselmonStorage.saveAndClear(it) }
    }
}
