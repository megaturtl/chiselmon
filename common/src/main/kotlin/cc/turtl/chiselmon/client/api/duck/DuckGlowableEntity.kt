package cc.turtl.chiselmon.client.api.duck

interface DuckGlowableEntity {
    fun `chiselmon$setClientGlowColor`(rgb: Int?)

    fun `chiselmon$setClientGlowing`(glowing: Boolean)

    fun `chiselmon$getClientGlowColor`(): Int?
}