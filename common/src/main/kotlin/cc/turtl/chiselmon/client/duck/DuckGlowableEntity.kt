package cc.turtl.chiselmon.client.duck

interface DuckGlowableEntity {
    fun `chiselmon$setClientGlowColor`(rgb: Int?)

    fun `chiselmon$setClientGlowing`(glowing: Boolean)

    fun `chiselmon$getClientGlowColor`(): Int?
}