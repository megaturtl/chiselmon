package cc.turtl.chiselmon.client.system.dashboard.api.endpoints

import cc.turtl.chiselmon.client.system.dashboard.api.ApiHandler
import cc.turtl.chiselmon.client.system.tracker.EncounterDatabase
import com.sun.net.httpserver.HttpExchange

class HeatmapHandler(db: EncounterDatabase) : ApiHandler(db) {
    private data class FetchBounds(
        val minX: Int,
        val maxX: Int,
        val minZ: Int,
        val maxZ: Int,
    )

    /**
     * Points are encoded as a flat interleaved array: `[x1, z1, x2, z2, ...]`.
     *
     * This halves the JSON payload vs `[{x, z}, ...]` by eliminating repeated key names —
     * significant at scale where heatmaps can return thousands of points.
     *
     * Client decodes with:
     * ```
     * for (let i = 0; i < arr.length; i += 2) use(arr[i], arr[i+1])
     * ```
     */
    private class HeatmapResponse(
        val cx: Int,
        val cz: Int,
        val radius: Int,
        val dimension: String,
        val fetchBounds: FetchBounds,
        val pokemon: IntArray,
        val player: IntArray,
    )

    override fun handle(exchange: HttpExchange) {
        handleRequest(exchange) { timeRange, params ->
            val cx = requireIntParam(params, "cx", 0)
            val cz = requireIntParam(params, "cz", 0)
            val radius = requireIntParam(params, "radius", 256).coerceIn(16, 4096)

            val dimension = params["dimension"] ?: "minecraft:overworld"

            val bounds = FetchBounds(cx - radius, cx + radius, cz - radius, cz + radius)
            val query =
                query("encounters")
                    .timeRange(timeRange)
                    .where("pokemon_x >= ?", bounds.minX)
                    .where("pokemon_x <= ?", bounds.maxX)
                    .where("pokemon_z >= ?", bounds.minZ)
                    .where("pokemon_z <= ?", bounds.maxZ)
                    .where("dimension = ?", dimension)

            val pokemon =
                query.select("pokemon_x, pokemon_z").fetchInterleavedPairs("pokemon_x", "pokemon_z")

            val player =
                query.select("player_x, player_z").fetchInterleavedPairs("player_x", "player_z")

            HeatmapResponse(cx, cz, radius, dimension, bounds, pokemon, player)
        }
    }
}
