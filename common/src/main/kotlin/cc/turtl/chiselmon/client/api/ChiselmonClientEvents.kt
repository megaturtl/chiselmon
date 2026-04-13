package cc.turtl.chiselmon.client.api

import cc.turtl.chiselmon.core.api.PokemonEncounter
import cc.turtl.chiselmon.core.api.predicate.IS_WILD
import cc.turtl.turtlshell.api.client.ClientEvents
import cc.turtl.turtlshell.impl.ObservableEvent
import com.cobblemon.mod.common.client.battle.ClientBattle
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity

data class PokemonLoadedEvent(val entity: PokemonEntity, val encounter: PokemonEncounter, val isWild: Boolean)
data class PokemonUnloadedEvent(val entity: PokemonEntity, val isWild: Boolean)
data class BattleStartedEvent(val battle: ClientBattle)
data class BattleEndedEvent(val battle: ClientBattle)

object ChiselmonClientEvents {
    val POKEMON_LOADED = ObservableEvent<PokemonLoadedEvent>()
    val POKEMON_UNLOADED = ObservableEvent<PokemonUnloadedEvent>()
    val BATTLE_STARTED = ObservableEvent<BattleStartedEvent>()
    val BATTLE_ENDED = ObservableEvent<BattleEndedEvent>()

    fun init() {
        ClientEvents.ENTITY_LOAD.subscribe { entity ->
            if (entity is PokemonEntity) {
                POKEMON_LOADED(PokemonLoadedEvent(entity, PokemonEncounter.from(entity), IS_WILD.test(entity)))
            }
        }
        ClientEvents.ENTITY_UNLOAD.subscribe { entity ->
            if (entity is PokemonEntity) {
                POKEMON_UNLOADED(PokemonUnloadedEvent(entity, IS_WILD.test(entity)))
            }
        }
    }
}