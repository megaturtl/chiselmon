package cc.turtl.chiselmon.client.system.alert

import cc.turtl.chiselmon.client.ChiselmonKeybinds
import cc.turtl.chiselmon.client.config.ChiselmonConfig
import cc.turtl.chiselmon.client.system.alert.action.DiscordAction
import cc.turtl.chiselmon.client.system.alert.action.MessageAction
import cc.turtl.chiselmon.client.system.alert.action.SoundAction
import cc.turtl.chiselmon.client.system.tracker.TrackerSession
import cc.turtl.chiselmon.client.util.addGlow
import cc.turtl.chiselmon.client.util.highlightNickname
import cc.turtl.chiselmon.client.util.sendSuccess
import cc.turtl.chiselmon.core.ChiselmonConstants
import cc.turtl.chiselmon.core.api.filter.match.FilterMatcher
import cc.turtl.turtlshell.api.client.ClientEvents
import net.minecraft.client.Minecraft
import java.util.*

object AlertManager {

    private const val SOUND_DELAY_TICKS = 20

    private val messageAction = MessageAction()
    private val soundAction = SoundAction()
    private val discordAction = DiscordAction()
    private val repeatingSoundAction = SoundAction()

    private val mutedUuids = hashSetOf<UUID>()
    private val actionedUuids = hashSetOf<UUID>()
    private var soundDelayRemaining = 0
    private var active = false

    fun init() {
        ClientEvents.LEVEL_CONNECTED.subscribe { onWorldJoin() }
        ClientEvents.LEVEL_DISCONNECTED.subscribe { onWorldLeave() }
        // low priority for the glow logic that might clash with despawn glow (alert should override despawn glow)
        ClientEvents.TICK_POST.subscribe { if (active) tick() }

        LureAlerter.init()

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

        while (ChiselmonKeybinds.MUTE_ALERTS.consumeClick()) {
            Minecraft.getInstance().player?.let { player ->
                muteAll()
                sendSuccess(player, "All active alert muted")
            }
        }

        // Track the "best" filter match for the sound this tick
        var bestSoundContext: AlertContext? = null

        for (pe in TrackerSession.current.currentlyLoaded.values) {
            val uuid = pe.uuid
            if (pe.busyLocks.isNotEmpty()) mute(uuid)

            val result = FilterMatcher.match(pe.pokemon)
            if (result.allMatches.isEmpty()) continue

            val ctx = AlertContext(
                entity = pe,
                filters = result.allMatches,
                isMuted = isMuted(uuid),
                config = config,
            )

            // Continuous: apply glow every tick
            val highlightFilter = ctx.highlightFilter
            if (ctx.shouldHighlight && highlightFilter != null) {
                pe.addGlow(highlightFilter.rgb)
                pe.highlightNickname(highlightFilter.rgb)
            }

            if (actionedUuids.add(uuid)) {
                messageAction.execute(ctx)
                soundAction.execute(ctx)
                discordAction.execute(ctx)
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

    fun muteAll() = mutedUuids.addAll(TrackerSession.current.currentlyLoaded.keys)

    fun unmuteAll() = mutedUuids.clear()

    fun isMuted(uuid: UUID): Boolean = uuid in mutedUuids

    fun getMutedUuids(): Set<UUID> = mutedUuids
}
