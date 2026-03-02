package cc.turtl.chiselmon.system.dashboard.api;

import cc.turtl.chiselmon.system.tracker.EncounterDatabase;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class for all API endpoint handlers.
 * <p>
 * Subclasses implement {@link #handle(HttpExchange)} by delegating to
 * {@link #handleRequest(HttpExchange, RequestHandler)}, which manages the full
 * request lifecycle: method validation, parameter parsing, serialization, and
 * error handling.
 * <p>
 * The {@link #query(String)} factory method is the primary entry point
 * for building database queries. It returns a {@link QueryBuilder}.
 * Handlers add their own conditions, ordering, and limits via the fluent API.
 */
public abstract class ApiHandler implements HttpHandler {

    protected static final Gson GSON = new GsonBuilder().create();

    protected final EncounterDatabase db;

    protected ApiHandler(EncounterDatabase db) {
        this.db = db;
    }

    /**
     * Functional interface for the inner request logic of each endpoint.
     * <p>
     * Receives a {@link TimeRange} and all decoded query parameters, and returns
     * any object to be serialized as the JSON response body.
     *
     * <p>May throw:
     * <ul>
     *   <li>{@link SQLException} for database errors → 500</li>
     *   <li>{@link IllegalArgumentException} for invalid request parameters → 400</li>
     * </ul>
     */
    @FunctionalInterface
    protected interface RequestHandler {
        Object handle(TimeRange timeRange, Map<String, String> params) throws SQLException;
    }

    /**
     * Executes a {@link RequestHandler} and writes the JSON response.
     * Handles GET validation, parameter parsing, serialization, and error responses.
     */
    protected void handleRequest(HttpExchange exchange, RequestHandler handler) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }
        try {
            Map<String, String> params = parseQuery(exchange.getRequestURI());
            TimeRange timeRange = TimeRange.from(params);
            Object result = handler.handle(timeRange, params);
            sendJson(exchange, 200, GSON.toJson(result));
        } catch (IllegalArgumentException e) {
            sendJson(exchange, 400, GSON.toJson(Map.of("error", e.getMessage())));
        } catch (SQLException e) {
            sendJson(exchange, 500, GSON.toJson(Map.of("error", e.getMessage())));
        }
    }

    /**
     * Creates a {@link QueryBuilder} for the given table.
     * Handlers chain {@link QueryBuilder#timeRange(TimeRange)} along with any
     * other conditions, ordering, and limits specific to their endpoint.
     *
     * @param table the table to query (e.g. {@code "encounters"})
     * @return a {@link QueryBuilder} ready for further chaining
     */
    protected QueryBuilder query(String table) {
        return new QueryBuilder(db.getConnection(), table);
    }

    /**
     * Parses an integer from the query parameter map, returning {@code defaultValue}
     * if the parameter is absent or not a valid integer.
     */
    protected static int parseIntParam(Map<String, String> params, String key, int defaultValue) {
        String value = params.get(key);
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Parses an integer from the query parameter map, throwing
     * {@link IllegalArgumentException} if the parameter is present but not a valid integer.
     */
    protected static int requireIntParam(Map<String, String> params, String key, int defaultValue) {
        String value = params.get(key);
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Parameter '" + key + "' must be an integer, got: " + value);
        }
    }

    protected void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    /**
     * Parses all query parameters into a map, URL-decoding both keys and values.
     * Required for dimension strings like {@code minecraft:the_nether} which arrive
     * percent-encoded as {@code minecraft%3Athe_nether}.
     */
    protected static Map<String, String> parseQuery(URI uri) {
        Map<String, String> map = new HashMap<>();
        String query = uri.getQuery();
        if (query == null) return map;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                try {
                    map.put(
                            URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                            URLDecoder.decode(kv[1], StandardCharsets.UTF_8)
                    );
                } catch (IllegalArgumentException e) {
                    map.put(kv[0], kv[1]);
                }
            }
        }
        return map;
    }
}