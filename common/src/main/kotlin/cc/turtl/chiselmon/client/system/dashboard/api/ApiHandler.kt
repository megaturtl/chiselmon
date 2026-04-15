package cc.turtl.chiselmon.client.system.dashboard.api

import cc.turtl.chiselmon.client.system.tracker.EncounterDatabase
import com.google.gson.Gson
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.sql.SQLException

/**
 * Base class for all API endpoint handlers.
 *
 * Subclasses implement [handle] by delegating to [handleRequest], which manages the full
 * request lifecycle: method validation, parameter parsing, serialization, and error handling.
 *
 * The [query] factory method is the primary entry point for building database queries.
 * Handlers add their own conditions, ordering, and limits via the fluent API.
 */
abstract class ApiHandler protected constructor(protected val db: EncounterDatabase) : HttpHandler {

    /**
     * Inner request logic for each endpoint. Receives a [TimeRange] and all decoded query
     * parameters, and returns any object to be serialized as the JSON response body.
     *
     * May throw:
     *   - [SQLException] for database errors → 500
     *   - [IllegalArgumentException] for invalid request parameters → 400
     */
    protected fun interface RequestHandler {
        @Throws(SQLException::class)
        fun handle(timeRange: TimeRange, params: Map<String, String>): Any
    }

    /**
     * Executes a [RequestHandler] and writes the JSON response. Handles GET validation,
     * parameter parsing, serialization, and error responses.
     */
    protected fun handleRequest(exchange: HttpExchange, handler: RequestHandler) {
        if (!"GET".equals(exchange.requestMethod, ignoreCase = true)) {
            exchange.sendResponseHeaders(405, -1)
            return
        }
        try {
            val params = parseQuery(exchange.requestURI)
            val timeRange = TimeRange.from(params)
            val result = handler.handle(timeRange, params)
            sendJson(exchange, 200, GSON.toJson(result))
        } catch (e: IllegalArgumentException) {
            sendJson(exchange, 400, GSON.toJson(mapOf("error" to e.message)))
        } catch (e: SQLException) {
            sendJson(exchange, 500, GSON.toJson(mapOf("error" to e.message)))
        }
    }

    /**
     * Creates a [QueryBuilder] for the given [table]. Handlers chain [QueryBuilder.timeRange]
     * along with any other conditions, ordering, and limits specific to their endpoint.
     */
    protected fun query(table: String): QueryBuilder = QueryBuilder(db.connection, table)

    /** Parse an int param, returning [defaultValue] if absent or malformed. */
    protected fun parseIntParam(params: Map<String, String>, key: String, defaultValue: Int): Int =
        params[key]?.toIntOrNull() ?: defaultValue

    /**
     * Parse an int param, throwing [IllegalArgumentException] if present but not a valid int.
     */
    protected fun requireIntParam(params: Map<String, String>, key: String, defaultValue: Int): Int {
        val value = params[key] ?: return defaultValue
        return value.toIntOrNull()
            ?: throw IllegalArgumentException("Parameter '$key' must be an integer, got: $value")
    }

    private fun sendJson(exchange: HttpExchange, status: Int, json: String) {
        val bytes = json.toByteArray(StandardCharsets.UTF_8)
        exchange.responseHeaders.set("Content-Type", "application/json; charset=utf-8")
        exchange.responseHeaders.set("Access-Control-Allow-Origin", "*")
        exchange.sendResponseHeaders(status, bytes.size.toLong())
        exchange.responseBody.use { it.write(bytes) }
    }

    /**
     * Parse all query parameters into a map, URL-decoding both keys and values.
     * Required for dimension strings like `minecraft:the_nether` which arrive
     * percent-encoded as `minecraft%3Athe_nether`.
     */
    private fun parseQuery(uri: URI): Map<String, String> {
        val query = uri.query ?: return emptyMap()
        val map = HashMap<String, String>()
        for (pair in query.split("&")) {
            val kv = pair.split("=", limit = 2)
            if (kv.size == 2) {
                try {
                    map[URLDecoder.decode(kv[0], StandardCharsets.UTF_8)] =
                        URLDecoder.decode(kv[1], StandardCharsets.UTF_8)
                } catch (_: IllegalArgumentException) {
                    map[kv[0]] = kv[1]
                }
            }
        }
        return map
    }

    companion object {
        private val GSON: Gson = Gson()
    }
}
