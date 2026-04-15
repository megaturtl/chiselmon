package cc.turtl.chiselmon.system.alert

import cc.turtl.chiselmon.client.ChiselmonKeybindsKt
import cc.turtl.chiselmon.client.config.ChiselmonConfig
import cc.turtl.chiselmon.client.util.sendSuccess
import cc.turtl.chiselmon.core.ChiselmonConstants
import cc.turtl.chiselmon.core.api.PokemonEncounter
import cc.turtl.chiselmon.core.api.filter.match.FilterMatcher
import cc.turtl.chiselmon.system.alert.action.*
import cc.turtl.chiselmon.system.tracker.TrackerManager
import cc.turtl.turtlshell.api.client.ClientEvents
import net.minecraft.client.Minecraft
import java.util.*

object AlertManager {

    private const val SOUND_DELAY_TICKS = 20

    private val oneTimeActions: List<AlertAction> = listOf(MessageAction(), SoundAction(), DiscordAction())
    private val continuousActions: List<AlertAction> = listOf(GlowAction())
    private val repeatingSoundAction = SoundAction()

    private val mutedUuids = hashSetOf<UUID>()
    private val actionedUuids = hashSetOf<UUID>()
    private var soundDelayRemaining = 0
    private var active = false

    fun init() {
        ClientEvents.LEVEL_CONNECTED.subscribe { onWorldJoin() }
        ClientEvents.LEVEL_DISCONNECTED.subscribe { onWorldLeave() }
        // low priority for the glow logic that might clash with despawn glow -- alert should override despawn glow
        ClientEvents.TICK_POST.subscribe { if (active) tick() }
        ChiselmonConstants.LOGGER.info("AlertSystem initialized")
    }

    private fun reset() {
        mutedUuids.clear()
        actionedUuids.clear()
        soundDelayRemaining = 0
    }

    private fun onWorldJoin() {
        if (active) {
            ChiselmonConstants.LOGGER.warn("New world joined before AlertSystem was disposed - resetting")
        }
        reset()
        active = true
        ChiselmonConstants.LOGGER.debug("AlertSystem started")
    }

    private fun onWorldLeave() {
        reset()
        active = false
        ChiselmonConstants.LOGGER.debug("AlertSystem disposed")
    }

    private fun tick() {
        val config = ChiselmonConfig.alert
        if (!config.masterEnabled) return

        while (ChiselmonKeybindsKt.MUTE_ALERTS.consumeClick()) {
            Minecraft.getInstance().player?.let { player ->
                muteAll()
                sendSuccess(player, "All active alert muted")
            }
        }

        // Track the "best" filter match for the sound this tick
        var bestSoundContext: AlertContext? = null

        for (pe in TrackerManager.tracker.currentlyLoaded.values) {
            val uuid = pe.uuid
            if (pe.busyLocks.isNotEmpty()) mute(uuid)

            val result = FilterMatcher.match(pe.pokemon)
            if (result.allMatches.isEmpty()) continue

            val ctx = AlertContext(
                entity = pe,
                filters = result.allMatches,
                isMuted = isMuted(uuid),
                config = config,
                encounter = PokemonEncounter.from(pe),
            )

            continuousActions.forEach { it.execute(ctx) }

            if (actionedUuids.add(uuid)) {
                oneTimeActions.forEach { it.execute(ctx) }
            }

            // Promote ctx to the best sound candidate if its filter out-prioritises the current best
            if (ctx.shouldRepeatingSound) {
                val currentBestPriority = bestSoundContext?.soundFilter?.priority
                val ctxPriority = ctx.soundFilter?.priority
                if (currentBestPriority == null || (ctxPriority != null && ctxPriority.isHigherThan(currentBestPriority))) {
                    bestSoundContext = ctx
                }
            }
        }

        // Replay the sound action for repeating sound alerts on a delay
        if (soundDelayRemaining > 0) {
            soundDelayRemaining--
        } else bestSoundContext?.let {
            repeatingSoundAction.executeRepeating(it)
            soundDelayRemaining = SOUND_DELAY_TICKS
        }
    }

    fun mute(uuid: UUID) = mutedUuids.add(uuid)

    fun muteAll() = mutedUuids.addAll(TrackerManager.tracker.currentlyLoaded.keys)

    fun unmuteAll() = mutedUuids.clear()

    fun isMuted(uuid: UUID): Boolean = uuid in mutedUuids

    fun getMutedUuids(): Set<UUID> = mutedUuids
}
