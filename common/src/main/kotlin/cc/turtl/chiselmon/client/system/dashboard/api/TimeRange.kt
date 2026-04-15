package cc.turtl.chiselmon.client.system.dashboard.api

/**
 * Optional time range filter derived from `from` and `to` query parameters (both epoch milliseconds).
 * Either bound may be absent (0 = unset = open-ended).
 *
 * Examples:
 *   /api/biomes                                           — all time
 *   /api/biomes?from=1700000000000                        — from timestamp onwards
 *   /api/biomes?from=1700000000000&to=1710000000000       — bounded range
 */
data class TimeRange(val from: Long, val to: Long) {

    val hasFrom: Boolean get() = from > 0
    val hasTo: Boolean get() = to > 0
    val isUnbounded: Boolean get() = !hasFrom && !hasTo

    companion object {
        /** Parse a [TimeRange] from an HTTP query-parameter map. Missing or malformed values become 0 (unset). */
        fun from(params: Map<String, String>): TimeRange =
            TimeRange(params["from"]?.toLongOrNull() ?: 0L, params["to"]?.toLongOrNull() ?: 0L)
    }
}
