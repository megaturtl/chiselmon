package cc.turtl.chiselmon.client.api.duck

import net.minecraft.network.chat.MutableComponent

interface DuckHighlightedPokemon {
    fun `chiselmon$setNicknameHighlight`(nickname: MutableComponent)

    fun `chiselmon$clearNicknameHighlight`()
}
