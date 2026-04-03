package cc.turtl.chiselmon.client.api

import cc.turtl.chiselmon.api.PokemonEncounter
import cc.turtl.chiselmon.api.predicate.PokemonEntityPredicates
import cc.turtl.turtlshell.api.client.ClientEvents
import cc.turtl.turtlshell.impl.ObservableEvent
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity

data class PokemonLoadedEvent(val entity: PokemonEntity, val encounter: PokemonEncounter, val isWild: Boolean)
data class PokemonUnloadedEvent(val entity: PokemonEntity, val isWild: Boolean)

object ChiselmonClientEvents {
    val POKEMON_LOADED = ObservableEvent<PokemonLoadedEvent>()
    val POKEMON_UNLOADED = ObservableEvent<PokemonUnloadedEvent>()

    fun init() {
        ClientEvents.ENTITY_LOAD.subscribe { entity ->
            if (entity is PokemonEntity) {
                POKEMON_LOADED(PokemonLoadedEvent(entity, PokemonEncounter.from(entity), PokemonEntityPredicates.IS_WILD.test(entity)))
            }
        }
        ClientEvents.ENTITY_UNLOAD.subscribe { entity ->
            if (entity is PokemonEntity) {
                POKEMON_UNLOADED(PokemonUnloadedEvent(entity, PokemonEntityPredicates.IS_WILD.test(entity)))
            }
        }
    }
}