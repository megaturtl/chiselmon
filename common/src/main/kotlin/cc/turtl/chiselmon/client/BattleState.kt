package cc.turtl.chiselmon.client

import cc.turtl.chiselmon.client.api.ChiselmonClientEvents
import com.cobblemon.mod.common.client.battle.ClientBattle

/** Client-side source of truth for the current battle state. */
object BattleState {

    var currentBattle: ClientBattle? = null
        private set

    /** Scraped from Cobblemon's battle message protocol. */
    var currentTurn: Int = 0
        private set

    fun init() {
        ChiselmonClientEvents.BATTLE_STARTED.subscribe { event ->
            currentBattle = event.battle
            currentTurn = 1
        }
        ChiselmonClientEvents.BATTLE_ENDED.subscribe {
            currentBattle = null
            currentTurn = 0
        }
    }

    fun onTurnMessage(turn: Int) {
        currentTurn = turn
    }
}