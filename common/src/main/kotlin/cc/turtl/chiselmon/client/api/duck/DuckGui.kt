package cc.turtl.chiselmon.client.api.duck

import net.minecraft.network.chat.Component

interface DuckGui {
    fun `chiselmon$clearOverlayMessage`(expected: Component)
}
