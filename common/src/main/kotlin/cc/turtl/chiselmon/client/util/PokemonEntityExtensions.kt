package cc.turtl.chiselmon.client.util

import cc.turtl.chiselmon.client.api.duck.DuckGlowableEntity
import cc.turtl.chiselmon.client.api.duck.DuckHighlightedPokemon
import cc.turtl.chiselmon.client.config.ChiselmonConfig
import cc.turtl.turtlshell.api.core.util.getClosestLegacy
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.network.chat.Component

fun PokemonEntity.addGlow(rgb: Int) {
    if (ChiselmonConfig.general.modDisabled) return
    val glowable = this as? DuckGlowableEntity ?: return
    glowable.`chiselmon$setClientGlowColor`(rgb)
}

fun PokemonEntity.removeGlow() {
    val glowable = this as? DuckGlowableEntity ?: return
    glowable.`chiselmon$setClientGlowColor`(null)
}

fun PokemonEntity.highlightNickname(rgb: Int) {
    if (ChiselmonConfig.general.modDisabled) return
    val mcColor = getClosestLegacy(rgb).char
    (pokemon as? DuckHighlightedPokemon)?.`chiselmon$setNicknameHighlight`(
        Component.literal("§${mcColor}${pokemon.species.name}"),
    )
}

fun PokemonEntity.resetNickname() {
    (pokemon as? DuckHighlightedPokemon)?.`chiselmon$clearNicknameHighlight`()
}
