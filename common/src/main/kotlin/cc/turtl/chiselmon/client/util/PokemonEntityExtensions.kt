package cc.turtl.chiselmon.client.util

import cc.turtl.chiselmon.client.api.duck.DuckGlowableEntity
import cc.turtl.turtlshell.api.core.util.getClosestLegacy
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.network.chat.Component

fun PokemonEntity.addGlow(rgb: Int) {
    val glowable = this as? DuckGlowableEntity ?: return
    glowable.`chiselmon$setClientGlowColor`(rgb)
    glowable.`chiselmon$setClientGlowing`(true)
}

fun PokemonEntity.removeGlow() {
    val glowable = this as? DuckGlowableEntity ?: return
    glowable.`chiselmon$setClientGlowing`(false)
}

fun PokemonEntity.highlightNickname(rgb: Int) {
    val mcColor = getClosestLegacy(rgb).char
    pokemon.nickname = Component.literal("§${mcColor}${pokemon.species.name}")
}

fun PokemonEntity.resetNickname() {
    pokemon.nickname = null
}